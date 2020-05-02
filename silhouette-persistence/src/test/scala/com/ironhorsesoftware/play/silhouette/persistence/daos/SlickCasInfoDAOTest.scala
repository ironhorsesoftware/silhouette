package com.ironhorsesoftware.play.silhouette.persistence.daos

import scala.reflect.classTag
import com.google.inject.Provides

import com.mohiva.play.silhouette.impl.providers.CasInfo

import org.specs2.mutable.Specification
import play.api.Application
import play.api.db.slick.DatabaseConfigProvider
import play.api.test.WithApplicationLoader
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.DurationInt

class RackRepositorySpec extends Specification {

  @Provides
  def provideCasInfoClassTag() = classTag[CasInfo]

  "RackRepository" should {
    "work as expected" in new WithApplicationLoader {

      val app2dao = Application.instanceCache[SlickCasInfoDAO]
      val rackRepository: SlickCasInfoDAO = app2dao(app)

       Await.result(rackRepository.createSchema(), 3 seconds)
    }
  }
}