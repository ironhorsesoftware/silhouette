package com.ironhorsesoftware.play.silhouette.persistence.daos

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag
import play.api.Logging
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.GoogleTotpInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO

import com.ironhorsesoftware.play.silhouette.persistence.model.authinfo.{GoogleTotpCredentials, GoogleTotpScratchCode}

class SlickGoogleTotpInfoDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec : ExecutionContext, val classTag : ClassTag[GoogleTotpInfo]) extends DelegableAuthInfoDAO[GoogleTotpInfo] with Logging {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class DbGoogleTotpCredentials(tag : Tag) extends Table[GoogleTotpCredentials](tag, "credentials_totp_google") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def providerId = column[String]("provider_id")
    def providerKey = column[String]("provider_key")
    def sharedKey = column[String]("shared_key")

    def * = (id, providerId, providerKey, sharedKey) <> (GoogleTotpCredentials.tupled, GoogleTotpCredentials.unapply)
  }

  private val credentials = TableQuery[DbGoogleTotpCredentials]

  private class DbGoogleTotpScratchCodes(tag : Tag) extends Table[GoogleTotpScratchCode](tag, "openid_attributes") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def googleTotpId = column[Int]("google_totp_id")
    def hasher = column[String]("hasher")
    def password = column[String]("password")
    def salt = column[Option[String]]("salt")

    def * = (id, googleTotpId, hasher, password, salt) <> (GoogleTotpScratchCode.tupled, GoogleTotpScratchCode.unapply)

    def googleTotpCredentials = foreignKey("google_totp_fkey", googleTotpId, credentials)(_.id)
  }

  private val attributes = TableQuery[DbGoogleTotpScratchCodes]  

  def add(loginInfo : LoginInfo, authInfo : GoogleTotpInfo) : Future[GoogleTotpInfo] = {
    find(loginInfo).flatMap { result =>
      result match {
        case Some(authInfo) => Future.successful(authInfo)
        case None => insert(loginInfo, authInfo)
      }
    }
  }

  def find(loginInfo : LoginInfo) : Future[Option[GoogleTotpInfo]] = db.run {
    credentials.join(attributes).on(_.id === _.googleTotpId).filter { case (creds, attrs) =>
      creds.providerId === loginInfo.providerID && creds.providerKey === loginInfo.providerKey
    }.result.map { rows =>
      rows.groupBy { case (creds, attrs) =>
        creds
      }.mapValues(values => values.map { case (creds, attrs) => attrs })
    }.map(result => result.map { case (creds, attrs) => GoogleTotpCredentials.buildAuthInfo(creds, attrs) })
  }.map(_.headOption)

  def remove(loginInfo : LoginInfo) : Future[Unit] = Future.failed(new UnsupportedOperationException)

  def save(loginInfo : LoginInfo, authInfo : GoogleTotpInfo) : Future[GoogleTotpInfo] = Future.failed(new UnsupportedOperationException)

  def update(loginInfo : LoginInfo, authInfo : GoogleTotpInfo) : Future[GoogleTotpInfo] = Future.failed(new UnsupportedOperationException)

  private def insert(loginInfo : LoginInfo, authInfo : GoogleTotpInfo) : Future[GoogleTotpInfo] = {
    val creds = GoogleTotpCredentials(loginInfo, authInfo)

    db.run {
      for {
        credId <- (credentials returning credentials.map(_.id)) += creds
        scratchCodes = authInfo.scratchCodes.map(scratchCode => GoogleTotpScratchCode(credId, scratchCode))
        // TODO: Add the scratch codes to the database.
      } yield credId
    }

    val scratchCodes = authInfo.scratchCodes.map(scratchCode => GoogleTotpScratchCode(0, scratchCode))
    Future.failed(new UnsupportedOperationException)
  }
}