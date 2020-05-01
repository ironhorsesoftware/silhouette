package com.ironhorsesoftware.play.silhouette.persistence

import scala.concurrent.Future

import org.scalatest.{BeforeAndAfterEach, Suite}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.SetParameter.SetUnit
import slick.jdbc.{JdbcProfile, SQLActionBuilder}

import scala.util.Try

trait DatabaseCleanerOnEachTest
    extends HasDatabaseConfigProvider[JdbcProfile]
    with BeforeAndAfterEach {
  this: Suite with InMemoryDatabaseFlatSpec =>

  override lazy val dbConfigProvider: DatabaseConfigProvider =
    app.injector.instanceOf[DatabaseConfigProvider]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    clearDatabase()
  }

  override protected def afterEach(): Unit = {
    clearDatabase()
    super.afterEach()
  }

  def clearDatabase(): Unit = {
    Try(dropTables())
    createTables()
  }

  def createTables()
  def dropTables()
}