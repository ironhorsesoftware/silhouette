package com.ironhorsesoftware.play.silhouette.persistence.utils

import play.api.libs.json._

object Serializers {
  def serializeMapToString(data : Map[String, String]) : String = {
    Json.toJson(data).toString
  }

  def deserializeMapFromString(str : String) : Map[String, String] = {
    Json.parse(str).as[JsObject].value.map { case (key, value) =>
      val item = value.asOpt[String]
      item match {
        case Some(str) => (key, str)
        case None => (key, item.toString)
      }
    }.toMap
  }
}