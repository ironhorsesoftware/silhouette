package com.ironhorsesoftware.play.silhouette.persistence.repositories

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.{Duration, DurationInt}

import play.api.Application
import play.api.db.slick.DatabaseConfigProvider
import play.api.test.WithApplicationLoader

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator

import org.joda.time.{DateTime, DateTimeZone}
import org.specs2.mutable.Specification

import scala.concurrent.ExecutionContext.Implicits.global

class SlickCookieAuthenticatorRespositorySpec extends Specification {

  "SlickCookieAuthenticatorRepository" should {
    "work as expected" in new WithApplicationLoader {

      val app2dao = Application.instanceCache[SlickCookieAuthenticatorRepository]
      val repository: SlickCookieAuthenticatorRepository = app2dao(app)

      val loginInfo = LoginInfo("providerId", "providerKey")
      val authenticator = CookieAuthenticator("id", loginInfo, DateTime.now().withZone(DateTimeZone.UTC), DateTime.now().withZone(DateTimeZone.UTC), None, None, Some("fingerprint"))
      
      Await.result(repository.createSchema(), 10 seconds)

      val createdAuthenticator = Await.result(repository.add(authenticator), 1 second)
      createdAuthenticator mustEqual authenticator

      val foundAuthenticatorOption = Await.result(repository.find(authenticator.id), 1  second)      
      foundAuthenticatorOption mustEqual Some(authenticator)

      val updatedAuthenticator = CookieAuthenticator(authenticator.id, loginInfo, DateTime.now().withZone(DateTimeZone.UTC), DateTime.now().withZone(DateTimeZone.UTC), Some(20 milliseconds), Some(30 milliseconds), Some("new fingerprint"))

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