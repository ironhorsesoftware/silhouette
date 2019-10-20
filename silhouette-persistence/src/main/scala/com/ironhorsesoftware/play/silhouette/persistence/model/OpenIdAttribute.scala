package com.ironhorsesoftware.play.silhouette.persistence.model

case class OpenIdAttribute(id : Int, openIdId : Int, attributeKey : String, attributeVal : String) {
  def attribute = (attributeKey -> attributeVal)
}
