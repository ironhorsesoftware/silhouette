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

class RackRepositorySpec extends Specification {

  "SlickCasInfoDAO" should {
    "work as expected" in new WithApplicationLoader {

      val app2dao = Application.instanceCache[SlickCasInfoDAO]
      val casInfoDAO: SlickCasInfoDAO = app2dao(app)

       Await.result(casInfoDAO.createSchema(), 10 seconds)

       val loginInfo = LoginInfo("providerId", "key")
       val authInfo = CasInfo("token")

       val createdCasInfo = Await.result(casInfoDAO.add(loginInfo, authInfo), 1 second)
       val foundCasInfoOption = Await.result(casInfoDAO.find(loginInfo), 1  second)

       val updatedCasInfo = CasInfo("newToken")
       val savedCasInfo = Await.result(casInfoDAO.save(loginInfo, updatedCasInfo), 1 second)

       val foundUpdatedCasInfo = Await.result(casInfoDAO.find(loginInfo), 1 second)

       Await.result(casInfoDAO.remove(loginInfo), 1 second)
 
       Await.result(casInfoDAO.dropSchema(), 10 seconds)
    }
  }
}