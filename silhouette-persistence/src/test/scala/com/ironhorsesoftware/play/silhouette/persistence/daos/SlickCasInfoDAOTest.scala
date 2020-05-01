package com.ironhorsesoftware.play.silhouette.persistence.daos

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Try

import org.scalatest.BeforeAndAfter
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CasInfo

import com.ironhorsesoftware.play.silhouette.persistence.DatabaseCleanerOnEachTest
import com.ironhorsesoftware.play.silhouette.persistence.InMemoryDatabaseFlatSpec

class SlickCasInfoDAOSpec extends AnyFlatSpec with GuiceOneAppPerSuite with BeforeAndAfter with Matchers {

  private val casInfoDAO = app.injector.instanceOf[SlickCasInfoDAO]

  def createTables() = {
     Await.result(casInfoDAO.createSchema(), Duration.Inf)
  }
  
  def dropTables() = {
    Await.result(casInfoDAO.dropSchema(), Duration.Inf)
  }

  "SlickCasInfoDAOSpec" should "blah" in {
    val loginInfo = LoginInfo("providerId", "providerKey")
    val authInfo = CasInfo("ticket")

    casInfoDAO.add(loginInfo, authInfo)
    casInfoDAO.find(loginInfo)
  }

}