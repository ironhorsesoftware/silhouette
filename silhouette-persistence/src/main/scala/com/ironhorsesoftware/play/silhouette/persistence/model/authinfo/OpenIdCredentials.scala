package com.ironhorsesoftware.play.silhouette.persistence.model.authinfo

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.OpenIDInfo

import com.ironhorsesoftware.play.silhouette.persistence.utils.Serializers

case class OpenIdCredentials(id : Int, providerId : String, providerKey : String, openId : String, attributes : String) {
  def loginInfo = LoginInfo(providerId, providerKey)

  def openIdInfo =
    OpenIDInfo(
        openId,
        Serializers.deserializeMapFromString(attributes))
}

object OpenIdCredentials extends Function5[Int, String, String, String, String, OpenIdCredentials] {
  def apply(loginInfo : LoginInfo, authInfo : OpenIDInfo) : OpenIdCredentials = {
    OpenIdCredentials(
        0,
        loginInfo.providerID,
        loginInfo.providerKey,
        authInfo.id,
        Serializers.serializeMapToString(authInfo.attributes))
  }
}
