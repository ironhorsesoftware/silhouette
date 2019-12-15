package com.ironhorsesoftware.play.silhouette.persistence.daos

import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

import play.api.Logging
import play.api.db.slick.DatabaseConfigProvider

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.OAuth1Info
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO

import com.ironhorsesoftware.play.silhouette.persistence.model.authinfo.OAuth1Credentials

class SlickOAuth1InfoDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec : ExecutionContext, implicit val classTag : ClassTag[OAuth1Info]) extends DelegableAuthInfoDAO[OAuth1Info] with Logging {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class DbOAuth1Credentials(tag : Tag) extends Table[OAuth1Credentials](tag, "credentials_oauth1") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def providerId = column[String]("provider_id")
    def providerKey = column[String]("provider_key")
    def token = column[String]("token")
    def secret = column[String]("secret")

    def * = (id, providerId, providerKey, token, secret) <> (OAuth1Credentials.tupled, OAuth1Credentials.unapply)
  }

  private val credentials = TableQuery[DbOAuth1Credentials]

  def add(loginInfo : LoginInfo, authInfo : OAuth1Info) : Future[OAuth1Info] = {
    val result =
      db.run {
        credentials.filter(creds => creds.providerKey === loginInfo.providerKey && creds.providerId === loginInfo.providerID).result.headOption.flatMap {
          case Some(credentials) => DBIO.successful(credentials.oauth1Info)
          case None => {
            for {
              _ <- credentials += OAuth1Credentials(loginInfo, authInfo)
            } yield authInfo
          }
        }
      }
    result
  }

  def find(loginInfo : LoginInfo) : Future[Option[OAuth1Info]] = db.run {
    credentials.filter(cred => cred.providerKey === loginInfo.providerKey && cred.providerId === loginInfo.providerID).result.headOption.map ( r => r.map { c =>
      c.oauth1Info
    })
  }

  def remove(loginInfo : LoginInfo) : Future[Unit] = db.run {
    val q = for { creds <- credentials if creds.providerId === loginInfo.providerID && creds.providerKey === loginInfo.providerKey } yield creds
    q.delete.map(_ => Unit)
  }

  def save(loginInfo : LoginInfo, authInfo : OAuth1Info) : Future[OAuth1Info] = db.run {
    val authCreds = OAuth1Credentials(loginInfo, authInfo)

    for {
      rowsAffected <- credentials.filter(c => c.providerId === authCreds.providerId && c.providerKey === authCreds.providerKey).map { oauth1Creds =>
          (oauth1Creds.secret, oauth1Creds.token)
        }.update((authCreds.secret, authCreds.token))
      result <- rowsAffected match {
          case 0 => credentials += authCreds
          case n => DBIO.successful(n)
        }
      queryResult <- credentials.filter(cred => cred.providerKey === authCreds.providerKey && cred.providerId === authCreds.providerId).result.head.map { c =>
        c.oauth1Info
      }
    } yield queryResult
  }

  def update(loginInfo : LoginInfo, authInfo : OAuth1Info) : Future[OAuth1Info] = db.run {
    for {
      numRowsAffected <- credentials.filter(cred => cred.providerId === loginInfo.providerID && cred.providerKey === loginInfo.providerKey).map { creds =>
          (creds.secret, creds.token)
        }.update((authInfo.secret, authInfo.token))
      result <- numRowsAffected match {
          case 0 => DBIO.failed(new IllegalArgumentException(s"No entries were found with provider ID ${loginInfo.providerID} and key ${loginInfo.providerKey}."))
          case _ => DBIO.successful(authInfo)
        }
    } yield result
  }  
}
