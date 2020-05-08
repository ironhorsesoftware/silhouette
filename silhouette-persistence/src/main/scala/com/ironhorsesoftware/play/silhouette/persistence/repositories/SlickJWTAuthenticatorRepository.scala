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
import com.ironhorsesoftware.play.silhouette.persistence.utils.DateTimeConverters

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
    def idleTimeout = column[Option[Long]]("idle_timeout")
    def customClaims = column[Option[String]]("custom_claims")

    def * = (id, authenticatorId, providerId, providerKey, lastUsedDateTime, expirationDateTime, idleTimeout, customClaims) <> (JWT.fromDatabaseRecord, JWT.toDatabaseRecord)
  }

  private val tokens = TableQuery[DbJWT]

  def createSchema() = db.run(tokens.schema.create)
  def dropSchema() : Future[Unit] = db.run(tokens.schema.drop)

  def add(authenticator : JWTAuthenticator) : Future[JWTAuthenticator] = {
    val result =
      db.run {
        tokens.filter(token => token.authenticatorId === authenticator.id).result.headOption.flatMap {
          case Some(existingAuthenticator) => DBIO.successful(existingAuthenticator.toJWTAuthenticator)
          case None => {
            for {
              _ <- tokens += JWT(authenticator)
            } yield authenticator
          }
        }
      }
    result
  }

  def find(id : String) : Future[Option[JWTAuthenticator]] = db.run {
    tokens.filter(_.authenticatorId === id).result.headOption.map(r => r.map { t =>
      t.toJWTAuthenticator
    })
  }

  def remove(id : String) : Future[Unit] = db.run {
    val q =  for { tkn <- tokens if tkn.authenticatorId === id } yield tkn
    q.delete.map(_ => ())
  }

  def update(authenticator : JWTAuthenticator) : Future[JWTAuthenticator] = db.run {
    val authenticatorToken = JWT(authenticator)

    for {
      numRowsAffected <- tokens.filter(_.authenticatorId === authenticatorToken.authenticatorId).map { token =>
          (token.providerId, token.providerKey, token.lastUsedDateTime, token.expirationDateTime, token.idleTimeout, token.customClaims)
        }.update(
            (authenticatorToken.providerId,
                authenticatorToken.providerKey,
                DateTimeConverters.dateTimeToTimestamp(authenticatorToken.lastUsedDateTime),
                DateTimeConverters.dateTimeToTimestamp(authenticatorToken.expirationDateTime),
                authenticatorToken.idleTimeout.map(DateTimeConverters.durationToMillis),
                authenticatorToken.customClaims.map(_.toString)
            )
        )
      result <- numRowsAffected match {
          case 0 => DBIO.failed(new IllegalArgumentException(s"No authenticators were found with ID ${authenticatorToken.authenticatorId}."))
          case n => DBIO.successful(authenticator)
        }
    } yield result
  }
}
