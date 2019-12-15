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
import com.mohiva.play.silhouette.impl.authenticators.BearerTokenAuthenticator

import com.ironhorsesoftware.play.silhouette.persistence.model.authenticator.BearerToken

class SlickBearerTokenAuthenticatorRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec : ExecutionContext) extends AuthenticatorRepository[BearerTokenAuthenticator] {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  
  import dbConfig._
  import profile.api._

  private class DbBearerToken(tag : Tag) extends Table[BearerToken](tag, "authentication_bearer_tokens") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def authenticatorId = column[String]("authenticator_id")
    def providerId = column[String]("provider_id")
    def providerKey = column[String]("provider_key")
    def lastUsedDateTime = column[Timestamp]("last_used_at")
    def expirationDateTime = column[Timestamp]("expires_at")
    def idleTimeout = column[Option[Time]]("idle_timeout")

    def * = (id, authenticatorId, providerId, providerKey, lastUsedDateTime, expirationDateTime, idleTimeout) <> (BearerToken.fromDatabaseRecord, BearerToken.toDatabaseRecord)
  }

  def add(authenticator : BearerTokenAuthenticator) = Future.failed(new UnsupportedOperationException)

  def find(id : String) = Future.failed(new UnsupportedOperationException)

  def remove(id : String) = Future.failed(new UnsupportedOperationException)

  def update(authenticator : BearerTokenAuthenticator) = Future.failed(new UnsupportedOperationException)
}
