package com.ironhorsesoftware.play.silhouette.impl.providers.oauth2

import scala.concurrent.{ExecutionContext, Future}

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.HTTPLayer
import com.mohiva.play.silhouette.impl.exceptions.ProfileRetrievalException
import com.mohiva.play.silhouette.impl.providers.{SocialProfileParser, SocialStateHandler, CommonSocialProfile, CommonSocialProfileBuilder}
import com.mohiva.play.silhouette.impl.providers.{OAuth2Settings, OAuth2Info}
import com.mohiva.play.silhouette.impl.providers.oauth2.BaseLinkedInProvider
import com.mohiva.play.silhouette.impl.providers.oauth2.LinkedInProvider.SpecifiedProfileError

import play.api.Logging
import play.api.libs.json.{JsValue, JsArray, JsNumber}

class LinkedInProvider(
    protected val httpLayer : HTTPLayer,
    protected val stateHandler : SocialStateHandler,
    val settings : OAuth2Settings) extends BaseLinkedInProvider with CommonSocialProfileBuilder {

  override type Self = LinkedInProvider

  override val id = LinkedInProvider.ID

  override protected val urls =
    Map("api" -> settings.apiURL.getOrElse(LinkedInProvider.API),
        "email" -> settings.customProperties.getOrElse("emailUrl", LinkedInProvider.EMAIL_URL))

  override val profileParser = new LinkedInProfileParser

  override protected def buildProfile(authInfo: OAuth2Info): Future[Profile] = {

    val emailAddrOptFuture =
      httpLayer.url(urls("email")).withHttpHeaders(("Authorization", s"Bearer ${authInfo.accessToken}")).get().flatMap { response =>
        val json = response.json
        (json \ "serviceErrorCode").asOpt[Int] match {
          case Some(errorCode) => Future.failed(buildError(json, errorCode))
          case _ => Future.successful(findEmailAddress(json))
        }
      }

    httpLayer.url(urls("api")).withHttpHeaders(("Authorization", s"Bearer ${authInfo.accessToken}")).get().flatMap { response =>
      val json = response.json
      (json \ "serviceErrorCode").asOpt[Int] match {
        case Some(errorCode) => Future.failed(buildError(json, errorCode))
        case _ => profileParser.parseWithEmailAddress(json, emailAddrOptFuture, authInfo)
      }
    }
  }

  override def withSettings(f : (Settings) => Settings) = {
    new LinkedInProvider(httpLayer, stateHandler, f(settings))
  }

  private def buildError(json : JsValue, errorCode : Int) : ProfileRetrievalException = {
    val message = (json \ "message").asOpt[String]
    val requestId = (json \ "requestId").asOpt[String]
    val status = (json \ "status").asOpt[Int]
    val timestamp = (json \ "timestamp").asOpt[Long]

    new ProfileRetrievalException(SpecifiedProfileError.format(id, errorCode, message, requestId, status, timestamp))
  }

  private def findEmailAddress(json : JsValue) : Option[String] = {
    (json \ "handle~" \ "emailAddress").asOpt[String].filter(item => !item.isEmpty)
  }
}

class LinkedInProfileParser extends SocialProfileParser[JsValue, CommonSocialProfile, OAuth2Info] {
  override def parse(json : JsValue, authInfo: OAuth2Info) = Future.successful {
    val userID = (json \ "id").as[String]
    val firstName = (json \ "localizedFirstName").asOpt[String].filter(item => !item.isEmpty)
    val lastName = (json \ "localizedLastName").asOpt[String].filter(item => !item.isEmpty)
    val fullName = (json \ "vanityName").asOpt[String].filter(item => !item.isEmpty)
    val avatarURL = findAvatarUrl(json)

    CommonSocialProfile(
      loginInfo = LoginInfo(LinkedInProvider.ID, userID),
      firstName = firstName,
      lastName = lastName,
      fullName = fullName,
      avatarURL = avatarURL,
      email = None)
  }

  def parseWithEmailAddress(json : JsValue, emailAddrOptFuture : Future[Option[String]], authInfo : OAuth2Info)(implicit ec : ExecutionContext) : Future[CommonSocialProfile] = {
    emailAddrOptFuture.flatMap { email =>
      parse(json, authInfo).map { profile =>
        profile.copy(email = email)
      }
    }
  }

  def parseCommonSocialProfile(json : JsValue, emailAddrOptFuture : Future[Option[String]], authInfo : OAuth2Info)(implicit ec : ExecutionContext) : Future[CommonSocialProfile] = {
    val userID = (json \ "id").as[String]
    val firstName = (json \ "localizedFirstName").asOpt[String].filter(item => !item.isEmpty)
    val lastName = (json \ "localizedLastName").asOpt[String].filter(item => !item.isEmpty)
    val fullName = (json \ "vanityName").asOpt[String].filter(item => !item.isEmpty)
    val avatarURL = findAvatarUrl(json)

    emailAddrOptFuture.map { email =>
      CommonSocialProfile(
        loginInfo = LoginInfo(LinkedInProvider.ID, userID),
        firstName = firstName,
        lastName = lastName,
        fullName = fullName,
        avatarURL = avatarURL,
        email = email)
    }
  }

  def findAvatarUrl(json : JsValue) : Option[String] = {
    (json \ "profilePicture" \ "displayImage~" \ "elements").as[JsArray].value.find { element =>
      (element \ "data" \ "com.linkedin.digitalmedia.mediaartifact.StillImage" \ "displaySize" \ "width").as[JsNumber].value >= 180 
    }.flatMap{ element =>
      (element \ "identifiers").as[JsArray].value.find(identifier => (identifier \ "index").as[JsNumber].value == 0) 
    }.map { identifier =>
      identifier \ "identifier"
    }.map { result =>
      result.as[String]
    }
  }
}

object LinkedInProvider {
  val ID = com.mohiva.play.silhouette.impl.providers.oauth2.LinkedInProvider.ID

  val API = "https://api.linkedin.com/v2/me?projection=(id,localizedFirstName,localizedLastName,localizedHeadline,vanityName,profilePicture(displayImage~playableStreams))"
  val EMAIL_URL = "https://api.linkedin.com/v2/emailAddress?q=members&projection=(elements*(handle~))"
}