package com.ironhorsesoftware.play.silhouette.persistence.model.authenticator

import java.sql.{Time, Timestamp}

import scala.concurrent.duration.FiniteDuration

import org.joda.time.{Instant, DateTime, DateTimeZone}

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.authenticators.BearerTokenAuthenticator

import com.ironhorsesoftware.play.silhouette.persistence.utils.DateTimeConverters

case class BearerToken (
    id : Int,
    authenticatorId : String,
    providerId : String,
    providerKey : String,
    lastUsedDateTime : DateTime,
    expirationDateTime : DateTime,
    idleTimeout : Option[FiniteDuration]
) {

  def toBearerTokenAuthenticator =
    new BearerTokenAuthenticator(
        authenticatorId,
        LoginInfo(providerId, providerKey),
        lastUsedDateTime,
        expirationDateTime,
        idleTimeout)
}

object BearerToken extends Function7[Int, String, String, String, DateTime, DateTime, Option[FiniteDuration], BearerToken] {

  def apply(authenticator : BearerTokenAuthenticator) =
    new BearerToken(
        0,
        authenticator.id,
        authenticator.loginInfo.providerID,
        authenticator.loginInfo.providerKey,
        authenticator.lastUsedDateTime,
        authenticator.expirationDateTime,
        authenticator.idleTimeout)

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
        DateTimeConverters.timestampToDateTime(lastUsedDateTime),
        DateTimeConverters.timestampToDateTime(expirationDateTime),
        idleTimeout.map(DateTimeConverters.timeToFiniteDuration))
  }

  def toDatabaseRecord(bearerToken : BearerToken) = {
    (bearerToken.id,
        bearerToken.authenticatorId,
        bearerToken.providerId,
        bearerToken.providerKey,
        DateTimeConverters.dateTimeToTimestamp(bearerToken.lastUsedDateTime),
        DateTimeConverters.dateTimeToTimestamp(bearerToken.expirationDateTime),
        bearerToken.idleTimeout.map(DateTimeConverters.durationToTime))
  }
}
