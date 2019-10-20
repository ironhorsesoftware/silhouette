package com.ironhorsesoftware.play.silhouette.persistence.daos

import scala.concurrent.Future

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.OAuth2Info
import com.mohiva.play.silhouette.persistence.daos.AuthInfoDAO

class SlickOAuth2InfoDAO extends AuthInfoDAO[OAuth2Info] {
  
  def add(loginInfo : LoginInfo, authInfo : OAuth2Info) = Future.failed(new UnsupportedOperationException)

  def find(loginInfo : LoginInfo) = Future.failed(new UnsupportedOperationException)

  def remove(loginInfo : LoginInfo) = Future.failed(new UnsupportedOperationException)

  def save(loginInfo : LoginInfo, authInfo : OAuth2Info) = Future.failed(new UnsupportedOperationException)

  def update(loginInfo: LoginInfo, authInfo : OAuth2Info) = Future.failed(new UnsupportedOperationException)
}