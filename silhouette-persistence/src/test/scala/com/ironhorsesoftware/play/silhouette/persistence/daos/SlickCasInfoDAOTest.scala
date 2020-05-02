package com.ironhorsesoftware.play.silhouette.persistence.daos

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.{Duration, DurationInt}

import play.api.Application
import play.api.db.slick.DatabaseConfigProvider
import play.api.test.WithApplicationLoader

import com.mohiva.play.silhouette.impl.providers.CasInfo

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CasInfo

import org.specs2.mutable.Specification

import scala.concurrent.ExecutionContext.Implicits.global

class SlickCASInfoDAOSpec extends Specification {

  "SlickCasInfoDAO" should {
    "work as expected" in new WithApplicationLoader {

      val app2dao = Application.instanceCache[SlickCasInfoDAO]
      val authInfoDAO: SlickCasInfoDAO = app2dao(app)
      val loginInfo = LoginInfo("providerId", "key")
      val authInfo = CasInfo("token")

      Await.result(authInfoDAO.createSchema(), 10 seconds)

      val createdAuthInfo = Await.result(authInfoDAO.add(loginInfo, authInfo), 1 second)
      createdAuthInfo mustEqual authInfo         

      val foundAuthInfoOption = Await.result(authInfoDAO.find(loginInfo), 1  second)      
      foundAuthInfoOption mustEqual Some(authInfo)

      val updatedAuthInfo = CasInfo("updatedToken")

      val savedAuthInfo = Await.result(authInfoDAO.save(loginInfo, updatedAuthInfo), 1 second)
      savedAuthInfo mustEqual updatedAuthInfo

      val foundUpdatedAuthInfo = Await.result(authInfoDAO.find(loginInfo), 1 second)
      foundUpdatedAuthInfo mustEqual Some(updatedAuthInfo)
     
      Await.result(authInfoDAO.remove(loginInfo), 1 second)
      val removedAuthInfoOption = Await.result(authInfoDAO.find(loginInfo), 1 second)
      removedAuthInfoOption mustEqual None

      val newAuthInfo = CasInfo("newToken")

      Await.result(authInfoDAO.update(loginInfo, newAuthInfo), 1 second) must throwA[IllegalArgumentException]

      val newlyCreatedAuthInfo = Await.result(authInfoDAO.save(loginInfo, newAuthInfo), 1 second)
      newlyCreatedAuthInfo mustEqual newAuthInfo

      val foundNewlyCreatedAuthInfo = Await.result(authInfoDAO.find(loginInfo), 1 second)
      foundNewlyCreatedAuthInfo mustEqual Some(newAuthInfo)

      Await.result(authInfoDAO.dropSchema(), 10 seconds)
    }
  }
}