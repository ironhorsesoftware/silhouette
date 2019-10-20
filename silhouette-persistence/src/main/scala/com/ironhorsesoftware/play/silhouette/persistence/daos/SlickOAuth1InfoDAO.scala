package com.ironhorsesoftware.play.silhouette.persistence.daos

import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

import play.api.Logging
import play.api.db.slick.DatabaseConfigProvider

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.OAuth1Info
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO

import com.ironhorsesoftware.play.silhouette.persistence.model.OAuth1Credentials

class SlickOAuth1InfoDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec : ExecutionContext, implicit val classTag : ClassTag[OAuth1Info]) extends DelegableAuthInfoDAO[OAuth1Info] with Logging {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class DbOAuth1Credentials(tag : Tag) extends Table[OAuth1Credentials](tag, "oauth1_credentials") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def providerId = column[String]("provider_id")
    def providerKey = column[String]("provider_key")
    def token = column[String]("token")
    def secret = column[String]("secret")

    def * = (id, providerId, providerKey, token, secret) <> (OAuth1Credentials.tupled, OAuth1Credentials.unapply)
  }

  private val credentials = TableQuery[DbOAuth1Credentials]

  def add(loginInfo : LoginInfo, authInfo : OAuth1Info) : Future[OAuth1Info] = Future.failed(new UnsupportedOperationException)

  def find(loginInfo : LoginInfo) : Future[Option[OAuth1Info]] = Future.failed(new UnsupportedOperationException)

  def remove(loginInfo : LoginInfo) : Future[Unit] = Future.failed(new UnsupportedOperationException)

  def save(loginInfo : LoginInfo, authInfo : OAuth1Info) : Future[OAuth1Info] = Future.failed(new UnsupportedOperationException)

  def update(loginInfo : LoginInfo, authInfo : OAuth1Info) : Future[OAuth1Info] = Future.failed(new UnsupportedOperationException)  
}
