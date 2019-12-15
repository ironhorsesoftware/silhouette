package com.ironhorsesoftware.play.silhouette.persistence.model.authinfo

import scala.util.Try

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.OAuth2Info

import com.ironhorsesoftware.play.silhouette.persistence.utils.Serializers

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

  def oauth2Info = OAuth2Info(accessToken, tokenType, expiresIn, refreshToken, params.map(Serializers.deserializeMapFromString))
}

object OAuth2Credentials extends Function8[Int, String, String, String, Option[String], Option[Int], Option[String], Option[String], OAuth2Credentials] {
  def apply(loginInfo : LoginInfo, oauth2Info : OAuth2Info) : OAuth2Credentials = {

    OAuth2Credentials(
        0,
        loginInfo.providerID,
        loginInfo.providerKey,
        oauth2Info.accessToken,
        oauth2Info.tokenType,
        oauth2Info.expiresIn,
        oauth2Info.refreshToken,
        oauth2Info.params.map(Serializers.serializeMapToString))
  }
}
