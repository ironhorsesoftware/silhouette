package com.ironhorsesoftware.play.silhouette.persistence

import javax.inject.Inject

import com.google.inject.{AbstractModule, Provides}

import net.codingwell.scalaguice.ScalaModule

import com.mohiva.play.silhouette.impl.providers.{OAuth1Info, OAuth2Info, OpenIDInfo, CasInfo}
import com.mohiva.play.silhouette.api.util.PasswordInfo

import com.mohiva.play.silhouette.persistence.daos.AuthInfoDAO

import com.ironhorsesoftware.play.silhouette.persistence.daos.{SlickCasInfoDAO, SlickPasswordInfoDAO}
import com.ironhorsesoftware.play.silhouette.persistence.daos.{SlickOAuth1InfoDAO, SlickOAuth2InfoDAO}
import com.ironhorsesoftware.play.silhouette.persistence.daos.SlickOpenIdDAO

class SlickPersistenceModule @Inject() extends AbstractModule with ScalaModule {

  override def configure {
    bind[AuthInfoDAO[CasInfo]].to[SlickCasInfoDAO]
    bind[AuthInfoDAO[PasswordInfo]].to[SlickPasswordInfoDAO]
    bind[AuthInfoDAO[OAuth1Info]].to[SlickOAuth1InfoDAO]
    bind[AuthInfoDAO[OAuth2Info]].to[SlickOAuth2InfoDAO]
    bind[AuthInfoDAO[OpenIDInfo]].to[SlickOpenIdDAO]
  }

  /*
  def provideAuthInfoRepository(
      casInfoDAO : AuthInfoDAO[CasInfo],
      passwordInfoDAO : AuthInfoDAO[PasswordInfo],
      oauth1InfoDAO : AuthInfoDAO[OAuth1Info],
      oauth2InfoDAO : AuthInfoDAO[OAuth2Info],
      openIdInfoDAO : AuthInfoDAO[OpenIDInfo]) = {
  }
  */
}