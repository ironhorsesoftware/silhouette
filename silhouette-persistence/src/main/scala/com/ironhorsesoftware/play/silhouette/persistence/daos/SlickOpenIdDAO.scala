package com.ironhorsesoftware.play.silhouette.persistence.daos

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag
import play.api.Logging
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.OpenIDInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import com.ironhorsesoftware.play.silhouette.persistence.model.authinfo.OpenIdCredentials


class SlickOpenIdDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec : ExecutionContext, implicit val classTag : ClassTag[OpenIDInfo]) extends DelegableAuthInfoDAO[OpenIDInfo] with Logging {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class DbOpenIdCredentials(tag : Tag) extends Table[OpenIdCredentials](tag, "credentials_openid") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def providerId = column[String]("provider_id")
    def providerKey = column[String]("provider_key")
    def openId = column[String]("openid")
    def attributes = column[String]("attributes")

    def * = (id, providerId, providerKey, openId, attributes) <> (OpenIdCredentials.tupled, OpenIdCredentials.unapply)
  }

  private val credentials = TableQuery[DbOpenIdCredentials]

  def add(loginInfo : LoginInfo, authInfo : OpenIDInfo) : Future[OpenIDInfo] = {
    val result =
      db.run {
        credentials.filter(creds => creds.providerKey === loginInfo.providerKey && creds.providerId === loginInfo.providerID).result.headOption.flatMap {
          case Some(credentials) => DBIO.successful(credentials.openIdInfo)
          case None => {
            for {
              _ <- credentials += OpenIdCredentials(loginInfo, authInfo)
            } yield authInfo
          }
        }
      }
    result
  }

  def find(loginInfo : LoginInfo) : Future[Option[OpenIDInfo]] = db.run {
    credentials.filter(cred => cred.providerKey === loginInfo.providerKey && cred.providerId === loginInfo.providerID).result.headOption.map ( r => r.map { c =>
      c.openIdInfo
    })
  }

  def remove(loginInfo : LoginInfo) : Future[Unit] = db.run {
    val q = for { creds <- credentials if creds.providerId === loginInfo.providerID && creds.providerKey === loginInfo.providerKey } yield creds
    q.delete.map(_ => Unit)
  }

  def save(loginInfo : LoginInfo, authInfo : OpenIDInfo) : Future[OpenIDInfo] = db.run {
    val authCreds = OpenIdCredentials(loginInfo, authInfo)

    for {
      rowsAffected <- credentials.filter(c => c.providerId === authCreds.providerId && c.providerKey === authCreds.providerKey).map { openIdCreds =>
          (openIdCreds.openId, openIdCreds.attributes)
        }.update((authCreds.openId, authCreds.attributes))
      result <- rowsAffected match {
          case 0 => credentials += authCreds
          case n => DBIO.successful(n)
        }
      queryResult <- credentials.filter(cred => cred.providerKey === authCreds.providerKey && cred.providerId === authCreds.providerId).result.head.map { c =>
        c.openIdInfo
      }
    } yield queryResult
  }

  def update(loginInfo : LoginInfo, authInfo : OpenIDInfo) : Future[OpenIDInfo] = db.run {
    val authCreds = OpenIdCredentials(loginInfo, authInfo)

    for {
      numRowsAffected <- credentials.filter(cred => cred.providerId === loginInfo.providerID && cred.providerKey === loginInfo.providerKey).map { creds =>
          (creds.openId, creds.attributes)
        }.update((authCreds.openId, authCreds.attributes))
      result <- numRowsAffected match {
          case 0 => DBIO.failed(new IllegalArgumentException(s"No entries were found with provider ID ${loginInfo.providerID} and key ${loginInfo.providerKey}."))
          case _ => DBIO.successful(authInfo)
        }
    } yield result
  }
}
