package com.ironhorsesoftware.play.silhouette.persistence.daos

import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

import play.api.Logging
import play.api.db.slick.DatabaseConfigProvider

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO

import com.ironhorsesoftware.play.silhouette.persistence.model.authinfo.PasswordCredentials

class SlickPasswordInfoDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec : ExecutionContext) extends DelegableAuthInfoDAO[PasswordInfo] with Logging {
  val classTag = scala.reflect.classTag[PasswordInfo]
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class DbPasswordCredentials(tag : Tag) extends Table[PasswordCredentials](tag, "credentials_password") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def providerId = column[String]("provider_id")
    def providerKey = column[String]("provider_key")
    def password = column[String]("password")
    def passwordHasher = column[String]("password_hasher")
    def passwordSalt = column[Option[String]]("password_salt")

    def * = (id, providerId, providerKey, password, passwordHasher, passwordSalt) <> (PasswordCredentials.tupled, PasswordCredentials.unapply)
  }

  private val credentials = TableQuery[DbPasswordCredentials]

  def createSchema() = db.run(credentials.schema.create)
  def dropSchema() : Future[Unit] = db.run(credentials.schema.drop)

  def add(loginInfo : LoginInfo, authInfo : PasswordInfo) : Future[PasswordInfo] = {
    val result =
      db.run {
        credentials.filter(creds => creds.providerKey === loginInfo.providerKey && creds.providerId === loginInfo.providerID).result.headOption.flatMap {
          case Some(credentials) => DBIO.successful(credentials.passwordInfo)
          case None => {
            for {
              _ <- credentials += PasswordCredentials(loginInfo, authInfo)  
            } yield authInfo
          }
        }
      }
    result
  }

  def find(loginInfo : LoginInfo) : Future[Option[PasswordInfo]] = db.run {
    credentials.filter(cred => cred.providerId === loginInfo.providerID && cred.providerKey === loginInfo.providerKey).result.headOption.map ( r => r.map { c =>
      c.passwordInfo
    })
  }

  def remove(loginInfo : LoginInfo) : Future[Unit] = db.run {
    val q = for { creds <- credentials if creds.providerId === loginInfo.providerID && creds.providerKey === loginInfo.providerKey } yield creds
    q.delete.map(_ => ())
  }

  def save(loginInfo : LoginInfo, authInfo : PasswordInfo) : Future[PasswordInfo] = db.run {
    val pwdCreds = PasswordCredentials(loginInfo, authInfo)

    for {
      rowsAffected <- credentials.filter(c => c.providerId === pwdCreds.providerId && c.providerKey === pwdCreds.providerKey).map { pwdCreds =>
          (pwdCreds.password, pwdCreds.passwordHasher, pwdCreds.passwordSalt)
        }.update((pwdCreds.password, pwdCreds.passwordHasher, pwdCreds.passwordSalt))
      result <- rowsAffected match {
          case 0 => credentials += pwdCreds
          case n => DBIO.successful(n)
        }
      queryResult <- credentials.filter(cred => cred.providerKey === pwdCreds.providerKey && cred.providerId === pwdCreds.providerId).result.head.map { c =>
        c.passwordInfo
      }
    } yield queryResult
  }

  def update(loginInfo : LoginInfo, authInfo : PasswordInfo) : Future[PasswordInfo] = db.run {
    for {
      numRowsAffected <- credentials.filter(cred => cred.providerId === loginInfo.providerID && cred.providerKey === loginInfo.providerKey).map { creds =>
          (creds.password, creds.passwordHasher, creds.passwordSalt)
        }.update((authInfo.password, authInfo.hasher, authInfo.salt))
      result <- numRowsAffected match {
          case 0 => DBIO.failed(new IllegalArgumentException(s"No entries were found with provider ID ${loginInfo.providerID} and key ${loginInfo.providerKey}."))
          case _ => DBIO.successful(authInfo)
        }
    } yield result
  }
}