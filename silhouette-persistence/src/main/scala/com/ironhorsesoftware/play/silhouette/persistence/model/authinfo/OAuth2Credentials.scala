package com.ironhorsesoftware.play.silhouette.persistence.model.authinfo

import scala.util.Try

import play.api.libs.json._

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.OAuth2Info

import play.api.Logging

case class OAuth2Credentials(
    id : Int,
    providerId : String,
    providerKey : String,
    accessToken : String,
    tokenType : Option[String],
    expiresIn : Option[Int],
    refreshToken : Option[String],
    params : Option[String]) {

  def loginInfo = LoginInfo(providerId, providerKey)

  def oauth2Info = OAuth2Info(accessToken, tokenType, expiresIn, refreshToken, params.map(OAuth2Credentials.stringToParams))
}

object OAuth2Credentials extends Function8[Int, String, String, String, Option[String], Option[Int], Option[String], Option[String], OAuth2Credentials] with Logging {
  def apply(loginInfo : LoginInfo, oauth2Info : OAuth2Info) : OAuth2Credentials = {

    OAuth2Credentials(
        0,
        loginInfo.providerID,
        loginInfo.providerKey,
        oauth2Info.accessToken,
        oauth2Info.tokenType,
        oauth2Info.expiresIn,
        oauth2Info.refreshToken,
        oauth2Info.params.map(paramsToString))
  }

  def paramsToString(params : Map[String, String]) = {
    Json.toJson(params).toString
  }

  def stringToParams(str : String) : Map[String, String] = {
    Json.parse(str).as[JsObject].value.map { case (key, value) =>
      (key, value.toString)
    }.toMap
  }
}
