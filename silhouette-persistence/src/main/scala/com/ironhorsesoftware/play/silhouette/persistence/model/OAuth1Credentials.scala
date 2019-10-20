package com.ironhorsesoftware.play.silhouette.persistence.model

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.OAuth1Info

case class OAuth1Credentials(id : Int, providerId : String, providerKey : String, token : String, secret : String) {
  def loginInfo = LoginInfo(providerId, providerKey) 
  def oauth1Info = OAuth1Info(token, secret)
}

object OAuth1Credentials extends Function5[Int, String, String, String, String, OAuth1Credentials] {
  def apply(loginInfo : LoginInfo, oauth1Info : OAuth1Info) : OAuth1Credentials = {
    OAuth1Credentials(0, loginInfo.providerID, loginInfo.providerKey, oauth1Info.token, oauth1Info.secret)
  }
}
