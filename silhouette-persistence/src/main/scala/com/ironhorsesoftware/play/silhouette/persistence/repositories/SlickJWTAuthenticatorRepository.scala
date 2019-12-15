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
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator

import com.ironhorsesoftware.play.silhouette.persistence.model.authenticator.JWT

class SlickJWTAuthenticatorRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec : ExecutionContext) extends AuthenticatorRepository[JWTAuthenticator] {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  
  import dbConfig._
  import profile.api._

  private class DbJWT(tag : Tag) extends Table[JWT](tag, "authentication_jwts") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def authenticatorId = column[String]("authenticator_id")
    def providerId = column[String]("provider_id")
    def providerKey = column[String]("provider_key")
    def lastUsedDateTime = column[Timestamp]("last_used_at")
    def expirationDateTime = column[Timestamp]("expires_at")
    def idleTimeout = column[Option[Time]]("idle_timeout")
    def customClaims = column[Option[String]]("custom_claims")

    def * = (id, authenticatorId, providerId, providerKey, lastUsedDateTime, expirationDateTime, idleTimeout, customClaims) <> (JWT.fromDatabaseRecord, JWT.toDatabaseRecord)
  }

  private val tokens = TableQuery[DbJWT]

  def add(authenticator : JWTAuthenticator) : Future[JWTAuthenticator] = Future.failed(new UnsupportedOperationException)

  def find(id : String) : Future[Option[JWTAuthenticator]] = Future.failed(new UnsupportedOperationException)

  def remove(id : String) : Future[Unit] = Future.failed(new UnsupportedOperationException)

  def update(authenticator : JWTAuthenticator) : Future[JWTAuthenticator] = Future.failed(new UnsupportedOperationException)
}
