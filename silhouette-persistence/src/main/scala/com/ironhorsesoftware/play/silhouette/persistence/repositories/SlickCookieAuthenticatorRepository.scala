package com.ironhorsesoftware.play.silhouette.persistence.repositories

import java.sql.{Time, Timestamp}
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

import play.api.Logging
import play.api.db.slick.DatabaseConfigProvider

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

import com.mohiva.play.silhouette.api.repositories.AuthenticatorRepository
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator

import com.ironhorsesoftware.play.silhouette.persistence.model.authenticator.Cookie

class SlickCookieAuthenticatorRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec : ExecutionContext) extends AuthenticatorRepository[CookieAuthenticator] {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class DbBearerToken(tag : Tag) extends Table[Cookie](tag, "authentication_cookies") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def authenticatorId = column[String]("authenticator_id")
    def providerId = column[String]("provider_id")
    def providerKey = column[String]("provider_key")
    def lastUsedDateTime = column[Timestamp]("last_used_at")
    def expirationDateTime = column[Timestamp]("expires_at")
    def idleTimeout = column[Option[Time]]("idle_timeout")
    def maxAge = column[Option[Time]]("max_age")
    def fingerprint = column[Option[String]]("fingerprint")

    def * = (id, authenticatorId, providerId, providerKey, lastUsedDateTime, expirationDateTime, idleTimeout, maxAge, fingerprint) <> (Cookie.fromDatabaseRecord, Cookie.toDatabaseRecord)
  }

  def add(authenticator : CookieAuthenticator) = Future.failed(new UnsupportedOperationException)

  def find(id : String) = Future.failed(new UnsupportedOperationException)

  def remove(id : String) = Future.failed(new UnsupportedOperationException)

  def update(authenticator : CookieAuthenticator) = Future.failed(new UnsupportedOperationException)
}
