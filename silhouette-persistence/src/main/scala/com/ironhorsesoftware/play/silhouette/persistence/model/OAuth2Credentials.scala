package com.ironhorsesoftware.play.silhouette.persistence.model

import scala.util.Try

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
    refreshToken : Option[String]) {

  def loginInfo = LoginInfo(providerId, providerKey)

  def oauth2Info = OAuth2Info(accessToken, tokenType, expiresIn, refreshToken, Some(Map(OAuth2Credentials.ID -> id.toString)))
}

object OAuth2Credentials extends Function7[Int, String, String, String, Option[String], Option[Int], Option[String], OAuth2Credentials] with Logging {
  def apply(loginInfo : LoginInfo, oauth2Info : OAuth2Info) : OAuth2Credentials = {

    OAuth2Credentials(
        Try(oauth2Info.params.getOrElse(Map()).get(ID).getOrElse(DEFAULT_ID_VALUE).toInt).getOrElse(0),
        loginInfo.providerID,
        loginInfo.providerKey,
        oauth2Info.accessToken,
        oauth2Info.tokenType,
        oauth2Info.expiresIn,
        oauth2Info.refreshToken)
  }

  val ID = "ID"
  val DEFAULT_ID_VALUE = "0"
}
