package com.ironhorsesoftware.play.silhouette.persistence.model.authenticator

import java.sql.{Time, Timestamp}

import scala.concurrent.duration.FiniteDuration

import org.joda.time.DateTime

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
      record : (Int, String, String, String, Timestamp, Timestamp, Option[Long])) = {

    BearerToken(
        record._1,
        record._2,
        record._3,
        record._4,
        DateTimeConverters.timestampToDateTime(record._5),
        DateTimeConverters.timestampToDateTime(record._6),
        record._7.map(DateTimeConverters.millisToFiniteDuration))
  }

  def toDatabaseRecord(bearerToken : BearerToken) = Some(
      (bearerToken.id,
        bearerToken.authenticatorId,
        bearerToken.providerId,
        bearerToken.providerKey,
        DateTimeConverters.dateTimeToTimestamp(bearerToken.lastUsedDateTime),
        DateTimeConverters.dateTimeToTimestamp(bearerToken.expirationDateTime),
        bearerToken.idleTimeout.map(DateTimeConverters.durationToMillis)))
}
