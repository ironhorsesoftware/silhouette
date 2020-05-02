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
      record : (Int, String, String, String, Timestamp, Timestamp, Option[Long], Option[String])) = {

    new JWT(
      record._1,
      record._2,
      record._3,
      record._4,
      DateTimeConverters.timestampToDateTime(record._5),
      DateTimeConverters.timestampToDateTime(record._6),
      record._7.map(DateTimeConverters.millisToFiniteDuration),
      record._8.map(claims => Json.parse(claims).as[JsObject]))
  }

  def toDatabaseRecord(jwt : JWT) = Some(
    (jwt.id,
        jwt.authenticatorId,
        jwt.providerId,
        jwt.providerKey,
        DateTimeConverters.dateTimeToTimestamp(jwt.lastUsedDateTime),
        DateTimeConverters.dateTimeToTimestamp(jwt.expirationDateTime),
        jwt.idleTimeout.map(DateTimeConverters.durationToMillis),
        jwt.customClaims.map(_.toString)))
}
