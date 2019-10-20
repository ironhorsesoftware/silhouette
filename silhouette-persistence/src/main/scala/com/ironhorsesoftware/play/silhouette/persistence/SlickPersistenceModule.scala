package com.ironhorsesoftware.play.silhouette.persistence

import javax.inject.Inject

import com.google.inject.{AbstractModule, Provides}

import net.codingwell.scalaguice.ScalaModule

import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.impl.providers.{OAuth1Info, OAuth2Info, OpenIDInfo, CasInfo, GoogleTotpInfo}
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.persistence.repositories.DelegableAuthInfoRepository

import com.ironhorsesoftware.play.silhouette.persistence.daos.{SlickCasInfoDAO, SlickPasswordInfoDAO}
import com.ironhorsesoftware.play.silhouette.persistence.daos.{SlickOAuth1InfoDAO, SlickOAuth2InfoDAO}
import com.ironhorsesoftware.play.silhouette.persistence.daos.{SlickOpenIdDAO, SlickGoogleTotpInfoDAO}

import scala.concurrent.ExecutionContext.Implicits.global

class SlickPersistenceModule @Inject() extends AbstractModule with ScalaModule {

  override def configure {
    bind[DelegableAuthInfoDAO[CasInfo]].to[SlickCasInfoDAO]
    bind[DelegableAuthInfoDAO[PasswordInfo]].to[SlickPasswordInfoDAO]
    bind[DelegableAuthInfoDAO[OAuth1Info]].to[SlickOAuth1InfoDAO]
    bind[DelegableAuthInfoDAO[OAuth2Info]].to[SlickOAuth2InfoDAO]
    bind[DelegableAuthInfoDAO[OpenIDInfo]].to[SlickOpenIdDAO]
    bind[DelegableAuthInfoDAO[GoogleTotpInfo]].to[SlickGoogleTotpInfoDAO]
  }

  @Provides
  def provideAuthInfoRepository(
      casInfoDAO : DelegableAuthInfoDAO[CasInfo],
      passwordInfoDAO : DelegableAuthInfoDAO[PasswordInfo],
      oauth1InfoDAO : DelegableAuthInfoDAO[OAuth1Info],
      oauth2InfoDAO : DelegableAuthInfoDAO[OAuth2Info],
      openIdInfoDAO : DelegableAuthInfoDAO[OpenIDInfo],
      googleTotpInfoDAO : DelegableAuthInfoDAO[GoogleTotpInfo]) : AuthInfoRepository = {

    new DelegableAuthInfoRepository(casInfoDAO, passwordInfoDAO, oauth1InfoDAO, oauth2InfoDAO, openIdInfoDAO, googleTotpInfoDAO)
  }
}