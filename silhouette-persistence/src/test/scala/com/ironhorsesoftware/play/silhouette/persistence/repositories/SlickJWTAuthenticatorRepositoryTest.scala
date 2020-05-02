package com.ironhorsesoftware.play.silhouette.persistence.repositories

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.{Duration, DurationInt}

import play.api.Application
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.{JsObject, JsString}
import play.api.test.WithApplicationLoader

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator

import org.joda.time.{DateTime, DateTimeZone}
import org.specs2.mutable.Specification

import scala.concurrent.ExecutionContext.Implicits.global

class SlickJWTAuthenticatorRespositorySpec extends Specification {

  "SlickJWTAuthenticatorRepository" should {
    "work as expected" in new WithApplicationLoader {

      val app2dao = Application.instanceCache[SlickJWTAuthenticatorRepository]
      val repository: SlickJWTAuthenticatorRepository = app2dao(app)

      val loginInfo = LoginInfo("providerId", "providerKey")
      val authenticator = JWTAuthenticator("id", loginInfo, DateTime.now().withZone(DateTimeZone.UTC), DateTime.now().withZone(DateTimeZone.UTC), None, None)
      
      Await.result(repository.createSchema(), 10 seconds)

      val createdAuthenticator = Await.result(repository.add(authenticator), 1 second)
      createdAuthenticator mustEqual authenticator

      val foundAuthenticatorOption = Await.result(repository.find(authenticator.id), 1  second)      
      foundAuthenticatorOption mustEqual Some(authenticator)

      val customClaims = JsObject(Seq(("item", JsString("value"))))

      val updatedAuthenticator = JWTAuthenticator(authenticator.id, loginInfo, DateTime.now().withZone(DateTimeZone.UTC), DateTime.now().withZone(DateTimeZone.UTC), Some(20 milliseconds), Some(customClaims))

      val savedAuthenticator = Await.result(repository.update(updatedAuthenticator), 1 second)
      savedAuthenticator mustEqual updatedAuthenticator

      val foundUpdatedAuthenticator = Await.result(repository.find(updatedAuthenticator.id), 1 second)
      foundUpdatedAuthenticator mustEqual Some(updatedAuthenticator)

      Await.result(repository.remove(updatedAuthenticator.id), 1 second)
      val removedAuthenticatorOption = Await.result(repository.find(updatedAuthenticator.id), 1 second)
      removedAuthenticatorOption mustEqual None

      Await.result(repository.update(authenticator), 1 second) must throwA[IllegalArgumentException]

      Await.result(repository.dropSchema(), 10 seconds)
    }
  }
}