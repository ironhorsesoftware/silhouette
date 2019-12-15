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

  private class DbGoogleTotpScratchCodes(tag : Tag) extends Table[GoogleTotpScratchCode](tag, "credentials_totp_google_scratch_codes") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def googleTotpId = column[Int]("google_totp_id")
    def hasher = column[String]("hasher")
    def password = column[String]("password")
    def salt = column[Option[String]]("salt")

    def * = (id, googleTotpId, hasher, password, salt) <> (GoogleTotpScratchCode.tupled, GoogleTotpScratchCode.unapply)

    def googleTotpCredentials = foreignKey("google_totp_fkey", googleTotpId, credentials)(_.id)
  }

  private val scratchCodes = TableQuery[DbGoogleTotpScratchCodes]  

  def add(loginInfo : LoginInfo, authInfo : GoogleTotpInfo) : Future[GoogleTotpInfo] = {
    find(loginInfo).flatMap { result =>
      result match {
        case Some(authInfo) => Future.successful(authInfo)
        case None => insert(loginInfo, authInfo)
      }
    }
  }

  def find(loginInfo : LoginInfo) : Future[Option[GoogleTotpInfo]] = db.run {
    credentials.join(scratchCodes).on(_.id === _.googleTotpId).filter { case (creds, attrs) =>
      creds.providerId === loginInfo.providerID && creds.providerKey === loginInfo.providerKey
    }.result.map { rows =>
      rows.groupBy { case (creds, attrs) =>
        creds
      }.mapValues(values => values.map { case (creds, attrs) => attrs })
    }.map(result => result.map { case (creds, attrs) => GoogleTotpCredentials.buildAuthInfo(creds, attrs) })
  }.map(_.headOption)

  def remove(loginInfo : LoginInfo) : Future[Unit] = {
    val credIdOptFuture =
      db.run {
        credentials.filter(cred => cred.providerId === loginInfo.providerID && cred.providerKey === loginInfo.providerKey).map(_.id).result.headOption
      }

    credIdOptFuture.flatMap(credIdOpt => credIdOpt match {
      case Some(totpId) => {
        val q1 = for { sc <- scratchCodes if sc.googleTotpId === totpId } yield sc
        val q2 = for { totp <- credentials if totp.id === totpId } yield totp
  
        db.run {
          DBIO.sequence(Seq(q1.delete, q2.delete)).transactionally
        }.map(_ => Unit)
      }
      case None => Future.successful(Unit)
    })
  }

  def save(loginInfo : LoginInfo, authInfo : GoogleTotpInfo) : Future[GoogleTotpInfo] = {
    find(loginInfo).flatMap { result =>
      result match {
        case Some(existingAuthInfo) => update(loginInfo, authInfo)
        case None => insert(loginInfo, authInfo)
      }
    }
  }

  def update(loginInfo : LoginInfo, authInfo : GoogleTotpInfo) : Future[GoogleTotpInfo] = {
    // 1. Find the corresponding ID
    val credIdOptFuture =
      db.run {
        credentials.filter(cred => cred.providerId === loginInfo.providerID && cred.providerKey === loginInfo.providerKey).map(_.id).result.headOption
      }

    // 2. Delete & re-insert the corresponding scratch tokens, then update the shared key.
    credIdOptFuture.flatMap(credIdOpt => credIdOpt match {
      case Some(totpId) => {
        val delete = (for { sc <- scratchCodes if sc.googleTotpId === totpId } yield sc).delete
        val insert = scratchCodes ++= authInfo.scratchCodes.map(scratchCode => GoogleTotpScratchCode(totpId, scratchCode))
        val update = credentials.filter(_.id === totpId).map(_.sharedKey).update(authInfo.sharedKey)
        db.run {
          DBIO.sequence(Seq(delete, insert, update)).transactionally
        }.map(_ => authInfo)
      }
      case None => Future.failed(new IllegalStateException(s"Cannot find a GoogleTotpInfo to update with the credentials ID ${loginInfo.providerID} and key ${loginInfo.providerKey}."))
    })
  }

  private def insert(loginInfo : LoginInfo, authInfo : GoogleTotpInfo) : Future[GoogleTotpInfo] = {
    val result =
      db.run {
        for {
          credId <- (credentials returning credentials.map(_.id)) += GoogleTotpCredentials(loginInfo, authInfo)
          scrtchCodes = authInfo.scratchCodes.map(scratchCode => GoogleTotpScratchCode(credId, scratchCode))
          _ <- scratchCodes ++= scrtchCodes
        } yield authInfo
      }
    result
  }
}