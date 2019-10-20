package com.ironhorsesoftware.play.silhouette.persistence.model

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.OpenIDInfo

case class OpenIdCredentials(id : Int, providerId : String, providerKey : String, openId : String) {
  def loginInfo = LoginInfo(providerId, providerKey)
}
