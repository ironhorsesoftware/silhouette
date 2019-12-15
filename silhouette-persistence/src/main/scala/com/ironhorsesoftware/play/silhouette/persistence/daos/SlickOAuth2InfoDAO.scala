package com.ironhorsesoftware.play.silhouette.persistence.daos

import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

import play.api.Logging
import play.api.db.slick.DatabaseConfigProvider

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.OAuth2Info
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO

import com.ironhorsesoftware.play.silhouette.persistence.model.authinfo.OAuth2Credentials

class SlickOAuth2InfoDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec : ExecutionContext, val classTag : ClassTag[OAuth2Info]) extends DelegableAuthInfoDAO[OAuth2Info] with Logging {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class DbOAuth2Credentials(tag : Tag) extends Table[OAuth2Credentials](tag, "credentials_oauth2") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def providerId = column[String]("provider_id")
    def providerKey = column[String]("provider_key")
    def accessToken = column[String]("access_token")
    def tokenType = column[Option[String]]("token_type")
    def expiresIn = column[Option[Int]]("expires_in")
    def refreshToken = column[Option[String]]("refresh_token")
    def params = column[Option[String]]("params")

    def * = (id, providerId, providerKey, accessToken, tokenType, expiresIn, refreshToken, params) <> (OAuth2Credentials.tupled, OAuth2Credentials.unapply)
  }

  private val credentials = TableQuery[DbOAuth2Credentials]

  def add(loginInfo : LoginInfo, authInfo : OAuth2Info) : Future[OAuth2Info] = {
    val result =
      db.run {
        credentials.filter(creds => creds.providerKey === loginInfo.providerKey && creds.providerId === loginInfo.providerID).result.headOption.flatMap {
          case Some(credentials) => DBIO.successful(credentials.oauth2Info)
          case None => {
            for {
              _ <- credentials += OAuth2Credentials(loginInfo, authInfo)  
            } yield authInfo
          }
        }
      }
    result
  }

  def find(loginInfo : LoginInfo) : Future[Option[OAuth2Info]] = db.run {
    credentials.filter(cred => cred.providerKey === loginInfo.providerKey && cred.providerId === loginInfo.providerID).result.headOption.map ( r => r.map { c =>
      c.oauth2Info
    })
  }

  def remove(loginInfo : LoginInfo) : Future[Unit] = db.run {
    val q = for { creds <- credentials if creds.providerId === loginInfo.providerID && creds.providerKey === loginInfo.providerKey } yield creds
    q.delete.map(_ => Unit)
  }

  def save(loginInfo : LoginInfo, authInfo : OAuth2Info) : Future[OAuth2Info] = db.run {
    val oauth2Creds = OAuth2Credentials(loginInfo, authInfo)

    for {
      rowsAffected <- credentials.filter(c => c.providerId === oauth2Creds.providerId && c.providerKey === oauth2Creds.providerKey).map { oauth2Creds =>
          (oauth2Creds.providerId, oauth2Creds.providerKey, oauth2Creds.accessToken, oauth2Creds.tokenType, oauth2Creds.expiresIn, oauth2Creds.refreshToken, oauth2Creds.params)
        }.update((oauth2Creds.providerId, oauth2Creds.providerKey, oauth2Creds.accessToken, oauth2Creds.tokenType, oauth2Creds.expiresIn, oauth2Creds.refreshToken, oauth2Creds.params))
      result <- rowsAffected match {
          case 0 => credentials += oauth2Creds
          case n => DBIO.successful(n)
        }
      queryResult <- credentials.filter(cred => cred.providerKey === oauth2Creds.providerKey && cred.providerId === oauth2Creds.providerId).result.head.map { c =>
        c.oauth2Info
      }
    } yield queryResult
  }

  def update(loginInfo : LoginInfo, authInfo : OAuth2Info) : Future[OAuth2Info] = db.run {
    val oauth2Creds = OAuth2Credentials(loginInfo, authInfo)

    for {
      rowsAffected <- credentials.filter(c => c.providerId === oauth2Creds.providerId && c.providerKey === oauth2Creds.providerKey).map { oauth2Creds =>
          (oauth2Creds.providerId, oauth2Creds.providerKey, oauth2Creds.accessToken, oauth2Creds.tokenType, oauth2Creds.expiresIn, oauth2Creds.refreshToken, oauth2Creds.params)
        }.update((oauth2Creds.providerId, oauth2Creds.providerKey, oauth2Creds.accessToken, oauth2Creds.tokenType, oauth2Creds.expiresIn, oauth2Creds.refreshToken, oauth2Creds.params))
      result <- rowsAffected match {
          case 0 => DBIO.failed(new IllegalArgumentException(s"No entries were found with provider ID ${loginInfo.providerID} and key ${loginInfo.providerKey}."))
          case n => DBIO.successful(authInfo)
        }
    } yield result
  }
}