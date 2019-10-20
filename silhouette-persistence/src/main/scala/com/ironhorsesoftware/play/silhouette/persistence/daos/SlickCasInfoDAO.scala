package com.ironhorsesoftware.play.silhouette.persistence.daos

import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import play.api.Logging
import play.api.db.slick.DatabaseConfigProvider

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CasInfo
import com.mohiva.play.silhouette.persistence.daos.AuthInfoDAO

import com.ironhorsesoftware.play.silhouette.persistence.model.CasCredentials

class SlickCasInfoDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec : ExecutionContext) extends AuthInfoDAO[CasInfo] with Logging {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class DbPasswordCredentials(tag : Tag) extends Table[CasCredentials](tag, "cas_credentials") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def providerId = column[String]("provider_id")
    def providerKey = column[String]("provider_key")
    def ticket = column[String]("ticket")

    def * = (id, providerId, providerKey, ticket) <> (CasCredentials.tupled, CasCredentials.unapply)
  }

  private val credentials = TableQuery[DbPasswordCredentials]

  def add(loginInfo : LoginInfo, authInfo : CasInfo) : Future[CasInfo] = Future.failed(new UnsupportedOperationException)

  def find(loginInfo : LoginInfo) : Future[Option[CasInfo]] = Future.failed(new UnsupportedOperationException)

  def remove(loginInfo : LoginInfo) : Future[Unit] = Future.failed(new UnsupportedOperationException)

  def save(loginInfo : LoginInfo, authInfo : CasInfo) : Future[CasInfo] = Future.failed(new UnsupportedOperationException)

  def update(loginInfo : LoginInfo, authInfo : CasInfo) : Future[CasInfo] = Future.failed(new UnsupportedOperationException)
}
