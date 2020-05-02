package com.ironhorsesoftware.play.silhouette.persistence.model.authenticator

import java.sql.{Time, Timestamp}

import scala.concurrent.duration.FiniteDuration

import org.joda.time.DateTime

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator

import com.ironhorsesoftware.play.silhouette.persistence.utils.DateTimeConverters

case class Cookie (
    id : Int,
    authenticatorId : String,
    providerId : String,
    providerKey : String,
    lastUsedDateTime : DateTime,
    expirationDateTime : DateTime,
    idleTimeout : Option[FiniteDuration],
    maxAge : Option[FiniteDuration],
    fingerprint : Option[String]

) {

  def toCookieAuthenticator =
    new CookieAuthenticator(
        authenticatorId,
        LoginInfo(providerId, providerKey),
        lastUsedDateTime,
        expirationDateTime,
        idleTimeout,
        maxAge,
        fingerprint)
}

object Cookie extends Function9[Int, String, String, String, DateTime, DateTime, Option[FiniteDuration], Option[FiniteDuration], Option[String], Cookie] {

  def apply(authenticator : CookieAuthenticator) =
    new Cookie(
        0,
        authenticator.id,
        authenticator.loginInfo.providerID,
        authenticator.loginInfo.providerKey,
        authenticator.lastUsedDateTime,
        authenticator.expirationDateTime,
        authenticator.idleTimeout,
        authenticator.cookieMaxAge,
        authenticator.fingerprint)

  def fromDatabaseRecord(
      record : (Int, String, String, String, Timestamp, Timestamp, Option[Long], Option[Long], Option[String])) = {

    new Cookie(
        record._1,
        record._2,
        record._3,
        record._4,
        DateTimeConverters.timestampToDateTime(record._5),
        DateTimeConverters.timestampToDateTime(record._6),
        record._7.map(DateTimeConverters.millisToFiniteDuration),
        record._8.map(DateTimeConverters.millisToFiniteDuration),
        record._9)
  }

  def toDatabaseRecord(cookie : Cookie) = Some(
    (cookie.id,
        cookie.authenticatorId,
        cookie.providerId,
        cookie.providerKey,
        DateTimeConverters.dateTimeToTimestamp(cookie.lastUsedDateTime),
        DateTimeConverters.dateTimeToTimestamp(cookie.expirationDateTime),
        cookie.idleTimeout.map(DateTimeConverters.durationToMillis),
        cookie.maxAge.map(DateTimeConverters.durationToMillis),
        cookie.fingerprint))
}
