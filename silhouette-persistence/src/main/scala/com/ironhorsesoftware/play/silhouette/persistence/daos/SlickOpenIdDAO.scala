package com.ironhorsesoftware.play.silhouette.persistence.daos

import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

import play.api.Logging
import play.api.db.slick.DatabaseConfigProvider

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.OpenIDInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO

import com.ironhorsesoftware.play.silhouette.persistence.model.{OpenIdCredentials, OpenIdAttribute}

class SlickOpenIdDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec : ExecutionContext, implicit val classTag : ClassTag[OpenIDInfo]) extends DelegableAuthInfoDAO[OpenIDInfo] with Logging {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class DbOpenIdCredentials(tag : Tag) extends Table[OpenIdCredentials](tag, "openid_credentials") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def providerId = column[String]("provider_id")
    def providerKey = column[String]("provider_key")
    def openId = column[String]("openid")

    def * = (id, providerId, providerKey, openId) <> (OpenIdCredentials.tupled, OpenIdCredentials.unapply)
  }

  private val credentials = TableQuery[DbOpenIdCredentials]

  private class DbOpenIdAttributes(tag : Tag) extends Table[OpenIdAttribute](tag, "openid_attributes") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def openIdId = column[Int]("openid_id")
    def attributeKey = column[String]("attribute_key")
    def attributeVal = column[String]("attribute_val")

    def * = (id, openIdId, attributeKey, attributeVal) <> (OpenIdAttribute.tupled, OpenIdAttribute.unapply)

    def openIdCredentials = foreignKey("openid_attributes_openid_fkey", openIdId, credentials)(_.id)
  }

  private val attributes = TableQuery[DbOpenIdAttributes]  

  def add(loginInfo : LoginInfo, authInfo : OpenIDInfo) : Future[OpenIDInfo] = Future.failed(new UnsupportedOperationException)

  def find(loginInfo : LoginInfo) : Future[Option[OpenIDInfo]] = Future.failed(new UnsupportedOperationException)

  def remove(loginInfo : LoginInfo) : Future[Unit] = Future.failed(new UnsupportedOperationException)

  def save(loginInfo : LoginInfo, authInfo : OpenIDInfo) : Future[OpenIDInfo] = Future.failed(new UnsupportedOperationException)

  def update(loginInfo : LoginInfo, authInfo : OpenIDInfo) : Future[OpenIDInfo] = Future.failed(new UnsupportedOperationException)
}
