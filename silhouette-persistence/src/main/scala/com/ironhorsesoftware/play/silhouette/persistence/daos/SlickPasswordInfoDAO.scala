package com.ironhorsesoftware.play.silhouette.persistence.daos

import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

import play.api.Logging
import play.api.db.slick.DatabaseConfigProvider

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO

import com.ironhorsesoftware.play.silhouette.persistence.model.PasswordCredentials

class SlickPasswordInfoDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec : ExecutionContext, val classTag : ClassTag[PasswordInfo]) extends DelegableAuthInfoDAO[PasswordInfo] with Logging {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class DbPasswordCredentials(tag : Tag) extends Table[PasswordCredentials](tag, "password_credentials") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def providerId = column[String]("provider_id")
    def providerKey = column[String]("provider_key")
    def password = column[String]("password")
    def passwordHasher = column[String]("password_hasher")
    def passwordSalt = column[Option[String]]("password_salt")

    def * = (id, providerId, providerKey, password, passwordHasher, passwordSalt) <> (PasswordCredentials.tupled, PasswordCredentials.unapply)
  }

  private val credentials = TableQuery[DbPasswordCredentials]

  def add(loginInfo : LoginInfo, authInfo : PasswordInfo) : Future[PasswordInfo] = db.run {
    for {
      _ <- (credentials += PasswordCredentials(loginInfo, authInfo))
    } yield authInfo
  }
  
  def find(loginInfo : LoginInfo) : Future[Option[PasswordInfo]] = db.run {
    credentials.filter(cred => cred.providerKey === loginInfo.providerKey).result.headOption.map ( r => r.map { c =>
      c.passwordInfo
    })
  }

  // TODO: Revisit this
  def remove(loginInfo : LoginInfo) : Future[Unit] = db.run {
    val q = for { creds <- credentials if creds.providerKey === loginInfo.providerKey } yield creds
    q.delete.map(_ => Unit)
  }

  // TODO: This is wrong
  def save(loginInfo : LoginInfo, authInfo : PasswordInfo) : Future[PasswordInfo] = db.run {
    for {
      _ <- credentials.insertOrUpdate(PasswordCredentials(loginInfo, authInfo))
    } yield authInfo
  }

  // TODO: This is wrong
  def update(loginInfo : LoginInfo, authInfo : PasswordInfo) : Future[PasswordInfo] = db.run {
    for {
      _ <- credentials.update(PasswordCredentials(loginInfo, authInfo))
    } yield authInfo
  }
}