package com.ironhorsesoftware.play.silhouette.persistence.repositories

import java.sql.Timestamp
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
import com.ironhorsesoftware.play.silhouette.persistence.utils.DateTimeConverters

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
    def idleTimeout = column[Option[Long]]("idle_timeout")

    def * = (id, authenticatorId, providerId, providerKey, lastUsedDateTime, expirationDateTime, idleTimeout) <> (BearerToken.fromDatabaseRecord, BearerToken.toDatabaseRecord)
  }

  private val tokens = TableQuery[DbBearerToken]

  def createSchema() = db.run(tokens.schema.create)
  def dropSchema() : Future[Unit] = db.run(tokens.schema.drop)

  def add(authenticator : BearerTokenAuthenticator) : Future[BearerTokenAuthenticator] = {
    val result =
      db.run {
        tokens.filter(token => token.authenticatorId === authenticator.id).result.headOption.flatMap {
          case Some(existingAuthenticator) => DBIO.successful(existingAuthenticator.toBearerTokenAuthenticator)
          case None => {
            for {
              _ <- tokens += BearerToken(authenticator)
            } yield authenticator
          }
        }
      }
    result
  }

  def find(id : String) : Future[Option[BearerTokenAuthenticator]] = db.run {
    tokens.filter(_.authenticatorId === id).result.headOption.map(r => r.map { t =>
      t.toBearerTokenAuthenticator
    })
  }

  def remove(id : String) : Future[Unit] = db.run {
    val q =  for { tkn <- tokens if tkn.authenticatorId === id } yield tkn
    q.delete.map(_ => ())
  }

  def update(authenticator : BearerTokenAuthenticator) : Future[BearerTokenAuthenticator] = db.run {
    val bearerToken = BearerToken(authenticator)

    for {
      numRowsAffected <- tokens.filter(_.authenticatorId === bearerToken.authenticatorId).map { token =>
          (token.providerId, token.providerKey, token.lastUsedDateTime, token.expirationDateTime, token.idleTimeout)
        }.update(
            (bearerToken.providerId,
                bearerToken.providerKey,
                DateTimeConverters.dateTimeToTimestamp(bearerToken.lastUsedDateTime),
                DateTimeConverters.dateTimeToTimestamp(bearerToken.expirationDateTime),
                bearerToken.idleTimeout.map(DateTimeConverters.durationToMillis)
            )
        )
      result <- numRowsAffected match {
          case 0 => DBIO.failed(new IllegalArgumentException(s"No authenticators were found with ID ${bearerToken.authenticatorId}."))
          case n => DBIO.successful(authenticator)
        }
    } yield result
  }
}
