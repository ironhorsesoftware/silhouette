package com.ironhorsesoftware.play.silhouette.persistence.model.authenticator

import java.sql.Time
import java.sql.Timestamp

import scala.concurrent.duration.{FiniteDuration, MILLISECONDS}

import org.joda.time.{Instant, DateTime, DateTimeZone}

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.authenticators.BearerTokenAuthenticator

case class BearerToken(
    id : Int,
    authenticatorId : String,
    providerId : String,
    providerKey : String,
    lastUsedDateTime : DateTime,
    expirationDateTime : DateTime,
    idleTimeout : Option[FiniteDuration]
) {

  def loginInfo = LoginInfo(providerId, providerKey)

  def toBearerTokenAuthenticator =
    new BearerTokenAuthenticator(authenticatorId, LoginInfo(providerId, providerKey), lastUsedDateTime, expirationDateTime, idleTimeout)
}

object BearerToken extends Function7[Int, String, String, String, DateTime, DateTime, Option[FiniteDuration], BearerToken] {

  def fromDatabaseRecord(
      id : Int,
      authenticatorId : String,
      providerId : String,
      providerKey : String,
      lastUsedDateTime : Timestamp,
      expirationDateTime : Timestamp,
      idleTimeout : Option[Time]) = {

    BearerToken(
        id,
        authenticatorId,
        providerId,
        providerKey,
        timestampToDateTime(lastUsedDateTime),
        timestampToDateTime(expirationDateTime),
        idleTimeout.map(it => FiniteDuration(it.getTime(), MILLISECONDS)))
  }

  def toDatabaseRecord(bearerToken : BearerToken) = {
    (bearerToken.id,
        bearerToken.authenticatorId,
        bearerToken.providerId,
        bearerToken.providerKey,
        dateTimeToTimestamp(bearerToken.lastUsedDateTime),
        dateTimeToTimestamp(bearerToken.expirationDateTime),
        bearerToken.idleTimeout.map(it => new Time(it.toMillis)))
  }

  private def timestampToDateTime(ts : Timestamp) = {
    Instant.ofEpochMilli(ts.getTime).toDateTime().withZone(DateTimeZone.UTC)
  }

  private def dateTimeToTimestamp(dt : DateTime) = {
    new Timestamp(dt.toInstant.getMillis)
  }
}
