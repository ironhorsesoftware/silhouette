package com.ironhorsesoftware.play.silhouette.persistence.model.authenticator

import java.sql.{Time, Timestamp}

import scala.concurrent.duration.FiniteDuration

import org.joda.time.DateTime

import play.api.libs.json.{Json, JsObject}

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator

import com.ironhorsesoftware.play.silhouette.persistence.utils.DateTimeConverters

case class JWT(
    id : Int,
    authenticatorId : String,
    providerId : String,
    providerKey : String,
    lastUsedDateTime : DateTime,
    expirationDateTime : DateTime,
    idleTimeout : Option[FiniteDuration],
    customClaims : Option[JsObject]
) {

  def toJWTAuthenticator =
    new JWTAuthenticator(
        authenticatorId,
        LoginInfo(providerId, providerKey),
        lastUsedDateTime,
        expirationDateTime,
        idleTimeout,
        customClaims)
}

object JWT extends Function8[Int, String, String, String, DateTime, DateTime, Option[FiniteDuration], Option[JsObject], JWT] {

  def apply(authenticator : JWTAuthenticator) =
    new JWT(
        0,
        authenticator.id,
        authenticator.loginInfo.providerID,
        authenticator.loginInfo.providerKey,
        authenticator.lastUsedDateTime,
        authenticator.expirationDateTime,
        authenticator.idleTimeout,
        authenticator.customClaims)

  def fromDatabaseRecord(
      id : Int,
      authenticatorId : String,
      providerId : String,
      providerKey : String,
      lastUsedDateTime : Timestamp,
      expirationDateTime : Timestamp,
      idleTimeout : Option[Time],
      customClaims : Option[String]) = {

    new JWT(
      id,
      authenticatorId,
      providerId,
      providerKey,
      DateTimeConverters.timestampToDateTime(lastUsedDateTime),
      DateTimeConverters.timestampToDateTime(expirationDateTime),
      idleTimeout.map(DateTimeConverters.timeToFiniteDuration),
      customClaims.map(claims => Json.parse(claims).as[JsObject]))
  }

  def toDatabaseRecord(jwt : JWT) = {
    (jwt.id,
        jwt.authenticatorId,
        jwt.providerId,
        jwt.providerKey,
        DateTimeConverters.dateTimeToTimestamp(jwt.lastUsedDateTime),
        DateTimeConverters.dateTimeToTimestamp(jwt.expirationDateTime),
        jwt.idleTimeout.map(DateTimeConverters.durationToTime),
        jwt.customClaims.map(_.toString))
  }
}
