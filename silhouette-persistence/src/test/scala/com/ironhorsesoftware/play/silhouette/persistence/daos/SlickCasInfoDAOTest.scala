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
      val casInfoDAO: SlickCasInfoDAO = app2dao(app)
      val loginInfo = LoginInfo("providerId", "key")
      val authInfo = CasInfo("token")

      Await.result(casInfoDAO.createSchema(), 10 seconds)

      "A new CasInfo should be added" >> {
        val createdCasInfo = Await.result(casInfoDAO.add(loginInfo, authInfo), 1 second)
        createdCasInfo mustEqual authInfo         
      }

      "The new CasInfo should be found" >> {
        val foundCasInfoOption = Await.result(casInfoDAO.find(loginInfo), 1  second)      
        foundCasInfoOption mustEqual Some(authInfo)
      }

      val updatedCasInfo = CasInfo("newToken")

      "Updating the CasInfo should save" >> {
        val savedCasInfo = Await.result(casInfoDAO.save(loginInfo, updatedCasInfo), 1 second)
        savedCasInfo mustEqual updatedCasInfo
      }

      "Should find the updated CasInfo" >> {
        val foundUpdatedCasInfo = Await.result(casInfoDAO.find(loginInfo), 1 second)
        foundUpdatedCasInfo mustEqual Some(updatedCasInfo)
      }
      
      "Should not be able to find the CasInfo after it was removed" >> {
        Await.result(casInfoDAO.remove(loginInfo), 1 second)
        val casInfoAfterRemovedOption = Await.result(casInfoDAO.find(loginInfo), 1 second)
        casInfoAfterRemovedOption mustEqual None
      }

      Await.result(casInfoDAO.dropSchema(), 10 seconds)
    }
  }
}