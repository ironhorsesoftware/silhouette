package com.ironhorsesoftware.play.silhouette.persistence.model

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo

case class PasswordCredentials(
    id : Int,
    providerId : String,
    providerKey : String,
    password : String,
    passwordHasher : String,
    passwordSalt : Option[String])
{
  def loginInfo = LoginInfo(providerId, providerKey)
  def passwordInfo = PasswordInfo(passwordHasher, password, passwordSalt)
}

object PasswordCredentials extends Function6[Int, String, String, String, String, Option[String], PasswordCredentials] {
  def apply(loginInfo : LoginInfo, passwordInfo : PasswordInfo) : PasswordCredentials = {
    PasswordCredentials(0, loginInfo.providerID, loginInfo.providerKey, passwordInfo.password, passwordInfo.hasher, passwordInfo.salt)
  }
}
