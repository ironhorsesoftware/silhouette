package com.ironhorsesoftware.play.silhouette.persistence.model.authinfo

case class OpenIdAttribute(id : Int, openIdId : Int, attributeKey : String, attributeVal : String) {
  def attribute = (attributeKey -> attributeVal)
}
