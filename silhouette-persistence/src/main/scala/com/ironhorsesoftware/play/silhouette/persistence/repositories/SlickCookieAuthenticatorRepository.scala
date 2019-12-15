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
import com.ironhorsesoftware.play.silhouette.persistence.utils.DateTimeConverters

class SlickCookieAuthenticatorRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec : ExecutionContext) extends AuthenticatorRepository[CookieAuthenticator] {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class DbCookie(tag : Tag) extends Table[Cookie](tag, "authentication_cookies") {
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

  private val tokens = TableQuery[DbCookie]

  def add(authenticator : CookieAuthenticator) : Future[CookieAuthenticator] = {
    val result =
      db.run {
        tokens.filter(token => token.authenticatorId === authenticator.id).result.headOption.flatMap {
          case Some(existingAuthenticator) => DBIO.successful(existingAuthenticator.toCookieAuthenticator)
          case None => {
            for {
              _ <- tokens += Cookie(authenticator)
            } yield authenticator
          }
        }
      }
    result
  }

  def find(id : String) : Future[Option[CookieAuthenticator]] = db.run {
    tokens.filter(_.authenticatorId === id).result.headOption.map(r => r.map { t =>
      t.toCookieAuthenticator
    })
  }

  def remove(id : String) : Future[Unit] = db.run {
    val q =  for { tkn <- tokens if tkn.authenticatorId === id } yield tkn
    q.delete.map(_ => Unit)
  }

  def update(authenticator : CookieAuthenticator) : Future[CookieAuthenticator] = db.run {
    val authenticatorToken = Cookie(authenticator)

    for {
      numRowsAffected <- tokens.filter(_.authenticatorId === authenticatorToken.authenticatorId).map { token =>
          (token.providerId, token.providerKey, token.lastUsedDateTime, token.expirationDateTime, token.idleTimeout, token.maxAge, token.fingerprint)
        }.update(
            (authenticatorToken.providerId,
                authenticatorToken.providerKey,
                DateTimeConverters.dateTimeToTimestamp(authenticatorToken.lastUsedDateTime),
                DateTimeConverters.dateTimeToTimestamp(authenticatorToken.expirationDateTime),
                authenticatorToken.idleTimeout.map(DateTimeConverters.durationToTime),
                authenticatorToken.maxAge.map(DateTimeConverters.durationToTime),
                authenticatorToken.fingerprint
            )
        )
      result <- numRowsAffected match {
          case 0 => DBIO.failed(new IllegalArgumentException(s"No authenticators were found with ID ${authenticatorToken.authenticatorId}."))
          case n => DBIO.successful(authenticator)
        }
    } yield result
  }
}
