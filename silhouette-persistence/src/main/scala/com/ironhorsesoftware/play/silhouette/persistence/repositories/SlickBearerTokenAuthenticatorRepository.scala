package com.ironhorsesoftware.play.silhouette.persistence.repositories

import scala.concurrent.{ExecutionContext, Future}

import com.mohiva.play.silhouette.api.repositories.AuthenticatorRepository
import com.mohiva.play.silhouette.impl.authenticators.BearerTokenAuthenticator

import com.ironhorsesoftware.play.silhouette.persistence.model.authenticator.BearerToken

class SlickBearerTokenAuthenticatorRepository extends AuthenticatorRepository[BearerTokenAuthenticator] {

  def add(authenticator : BearerTokenAuthenticator) = Future.failed(new UnsupportedOperationException)

  def find(id : String) = Future.failed(new UnsupportedOperationException)

  def remove(id : String) = Future.failed(new UnsupportedOperationException)

  def update(authenticator : BearerTokenAuthenticator) = Future.failed(new UnsupportedOperationException)
}