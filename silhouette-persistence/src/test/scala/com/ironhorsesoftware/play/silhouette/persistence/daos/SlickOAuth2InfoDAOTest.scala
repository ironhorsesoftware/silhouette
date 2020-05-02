package com.ironhorsesoftware.play.silhouette.persistence.daos

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.{Duration, DurationInt}

import play.api.Application
import play.api.db.slick.DatabaseConfigProvider
import play.api.test.WithApplicationLoader

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.OAuth2Info

import org.specs2.mutable.Specification

import scala.concurrent.ExecutionContext.Implicits.global

class SlickOAuth2InfoDAOSpec extends Specification {

  "SlickOAuth2InfoDAO" should {
    "work as expected" in new WithApplicationLoader {

      val app2dao = Application.instanceCache[SlickOAuth2InfoDAO]
      val authInfoDAO: SlickOAuth2InfoDAO = app2dao(app)
      val loginInfo = LoginInfo("providerId", "key")
      val authInfo = OAuth2Info("token", Some("tokenType"), Some(20), Some("refreshToken"))

      Await.result(authInfoDAO.createSchema(), 10 seconds)

      val createdAuthInfo = Await.result(authInfoDAO.add(loginInfo, authInfo), 1 second)
      createdAuthInfo mustEqual authInfo         

      val foundAuthInfoOption = Await.result(authInfoDAO.find(loginInfo), 1  second)      
      foundAuthInfoOption mustEqual Some(authInfo)

      val updatedAuthInfo = OAuth2Info("updatedToken")

      val savedAuthInfo = Await.result(authInfoDAO.save(loginInfo, updatedAuthInfo), 1 second)
      savedAuthInfo mustEqual updatedAuthInfo

      val foundUpdatedAuthInfo = Await.result(authInfoDAO.find(loginInfo), 1 second)
      foundUpdatedAuthInfo mustEqual Some(updatedAuthInfo)
     
      Await.result(authInfoDAO.remove(loginInfo), 1 second)
      val removedAuthInfoOption = Await.result(authInfoDAO.find(loginInfo), 1 second)
      removedAuthInfoOption mustEqual None

      val newAuthInfo = OAuth2Info("newToken", Some("newTokenType"), Some(40), Some("newRefreshToken"), Some(Map("param1" -> "value1")))

      Await.result(authInfoDAO.update(loginInfo, newAuthInfo), 1 second) must throwA[IllegalArgumentException]

      val newlyCreatedAuthInfo = Await.result(authInfoDAO.save(loginInfo, newAuthInfo), 1 second)
      newlyCreatedAuthInfo mustEqual newAuthInfo

      val foundNewlyCreatedAuthInfo = Await.result(authInfoDAO.find(loginInfo), 1 second)
      foundNewlyCreatedAuthInfo mustEqual Some(newAuthInfo)

      Await.result(authInfoDAO.dropSchema(), 10 seconds)
    }
  }
}