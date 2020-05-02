package com.ironhorsesoftware.play.silhouette.persistence.daos

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag
import play.api.Logging
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CasInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import com.ironhorsesoftware.play.silhouette.persistence.model.authinfo.CasCredentials
import slick.lifted.ProvenShape.proveShapeOf

class SlickCasInfoDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec : ExecutionContext) extends DelegableAuthInfoDAO[CasInfo] with Logging {
  val classTag = scala.reflect.classTag[CasInfo]
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class DbCasCredentials(tag : Tag) extends Table[CasCredentials](tag, "credentials_cas") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def providerId = column[String]("provider_id")
    def providerKey = column[String]("provider_key")
    def ticket = column[String]("ticket")

    def * = (id, providerId, providerKey, ticket) <> (CasCredentials.tupled, CasCredentials.unapply)
  }

  private val credentials = TableQuery[DbCasCredentials]

  def createSchema() = db.run(credentials.schema.create)
  def dropSchema() : Future[Unit] = db.run(credentials.schema.drop)

  def add(loginInfo : LoginInfo, authInfo : CasInfo) : Future[CasInfo] = {
    val result =
      db.run {
        credentials.filter(creds => creds.providerKey === loginInfo.providerKey && creds.providerId === loginInfo.providerID).result.headOption.flatMap {
          case Some(credentials) => DBIO.successful(credentials.casInfo)
          case None => {
            for {
              _ <- credentials += CasCredentials(loginInfo, authInfo)
            } yield authInfo
          }
        }
      }
    result
  }

  def find(loginInfo : LoginInfo) : Future[Option[CasInfo]] = db.run {
    credentials.filter(cred => cred.providerKey === loginInfo.providerKey && cred.providerId === loginInfo.providerID).result.headOption.map ( r => r.map { c =>
      c.casInfo
    })
  }

  def remove(loginInfo : LoginInfo) : Future[Unit] = db.run {
    val q = for { creds <- credentials if creds.providerId === loginInfo.providerID && creds.providerKey === loginInfo.providerKey } yield creds
    q.delete.map(_ => Unit)
  }

  def save(loginInfo : LoginInfo, authInfo : CasInfo) : Future[CasInfo] = db.run {
    val authCreds = CasCredentials(loginInfo, authInfo)

    for {
      rowsAffected <- credentials.filter(c => c.providerId === authCreds.providerId && c.providerKey === authCreds.providerKey).map { casCreds =>
          casCreds.ticket
        }.update(authCreds.ticket)
      result <- rowsAffected match {
          case 0 => credentials += authCreds
          case n => DBIO.successful(n)
        }
      queryResult <- credentials.filter(cred => cred.providerKey === authCreds.providerKey && cred.providerId === authCreds.providerId).result.head.map { c =>
        c.casInfo
      }
    } yield queryResult
  }

  def update(loginInfo : LoginInfo, authInfo : CasInfo) : Future[CasInfo] = db.run {
    for {
      numRowsAffected <- credentials.filter(cred => cred.providerId === loginInfo.providerID && cred.providerKey === loginInfo.providerKey).map { creds =>
          creds.ticket
        }.update(authInfo.ticket)
      result <- numRowsAffected match {
          case 0 => DBIO.failed(new IllegalArgumentException(s"No entries were found with provider ID ${loginInfo.providerID} and key ${loginInfo.providerKey}."))
          case _ => DBIO.successful(authInfo)
        }
    } yield result
  }
}
