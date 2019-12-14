package com.ironhorsesoftware.play.silhouette.persistence.model.authinfo

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.GoogleTotpInfo
import com.ironhorsesoftware.play.silhouette.persistence.model.GoogleTotpScratchCode
import com.ironhorsesoftware.play.silhouette.persistence.model.authinfo.GoogleTotpScratchCode

case class GoogleTotpCredentials(id : Int, providerId : String, providerKey : String, sharedKey : String) {
  def loginInfo = LoginInfo(providerId, providerKey)
}

object GoogleTotpCredentials extends Function4[Int, String, String, String, GoogleTotpCredentials] {
  def apply(loginInfo : LoginInfo, authInfo : GoogleTotpInfo) : GoogleTotpCredentials = {
    GoogleTotpCredentials(0, loginInfo.providerID, loginInfo.providerKey, authInfo.sharedKey)
  }

  def buildAuthInfo(credentials : GoogleTotpCredentials, scratchCodes : Seq[GoogleTotpScratchCode]) = {
    GoogleTotpInfo(credentials.sharedKey, scratchCodes.map(sc => sc.passwordInfo))
  }
}
