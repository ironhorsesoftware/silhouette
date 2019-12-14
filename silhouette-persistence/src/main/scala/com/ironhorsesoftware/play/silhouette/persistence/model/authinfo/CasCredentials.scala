package com.ironhorsesoftware.play.silhouette.persistence.model.authinfo

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CasInfo

case class CasCredentials(id : Int, providerId : String, providerKey : String, ticket : String) {
  def loginInfo = LoginInfo(providerId, providerKey)
  def casInfo = CasInfo(ticket)
}

object CasCredentials extends Function4[Int, String, String, String, CasCredentials] {
  def apply(loginInfo : LoginInfo, casInfo : CasInfo) : CasCredentials = {
    CasCredentials(0, loginInfo.providerID, loginInfo.providerKey, casInfo.ticket)
  }
}
