package com.ironhorsesoftware.play.silhouette.persistence.model

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.OpenIDInfo

case class OpenIdCredentials(id : Int, providerId : String, providerKey : String, openId : String) {
  def loginInfo = LoginInfo(providerId, providerKey)
}

object OpenIdCredentials extends Function4[Int, String, String, String, OpenIdCredentials] {
  def apply(loginInfo : LoginInfo, authInfo : OpenIDInfo) : OpenIdCredentials = {
    OpenIdCredentials(0, loginInfo.providerID, loginInfo.providerKey, authInfo.id)
  }

  def buildAuthInfo(credentials: OpenIdCredentials, attributes : Seq[OpenIdAttribute]) = {
    OpenIDInfo(credentials.openId, attributes.map(attr => attr.attribute).toMap)
  }
}
