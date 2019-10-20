package com.ironhorsesoftware.play.silhouette.persistence.model

import com.mohiva.play.silhouette.api.util.PasswordInfo

case class GoogleTotpScratchCode(id : Int, googleTotpId : Int, hasher : String, password : String, salt : Option[String]) {
  def passwordInfo = PasswordInfo(hasher, password, salt)
}

object GoogleTotpScratchCode extends Function5[Int, Int, String, String, Option[String], GoogleTotpScratchCode] {
  def apply(googleTotpId : Int, passwordInfo : PasswordInfo) : GoogleTotpScratchCode = {
    GoogleTotpScratchCode(0, googleTotpId, passwordInfo.hasher, passwordInfo.password, passwordInfo.salt)
  }
}
