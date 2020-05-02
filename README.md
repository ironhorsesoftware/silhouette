# Clock

The `silhouette` project provides an alternate binding for `Clock` which uses the Java `LocalDateTime` instead of the Joda `DateTime`.

# Silhouette + Slick

The `silhouette-persistence` project provides bindings to persist all of the Silhouette `AuthInfo` and `StorableAuthenticator` (excluding `FakeAuthenticator`) objects to a relational database using Slick.

## Usage

### AuthInfo

Silhouette provides the following classes to store the authentication information for each [Silhouette provider](https://www.silhouette.rocks/docs/providers):

1. `CasInfo`
6. `GoogleTotpInfo`
3. `OAuth1Info`
4. `OAuth2Info`
5. `OpenIDInfo`
2. `PasswordInfo`

There are two ways to bind these `AuthInfo` implementations in your project to the provided Slick instances.  The first is to enable the provided `SlickPersistenceModule`, which will define an instance of `DelegableAuthInfoRepository` with all of the instances bound.  The second way is to bind the individual DAOs in your own module and constructing the `DelegableAuthInfoRepository` yourself.

#### DelegableAuthInfoRepository

Adding the `SlickPersistenceModule` to a Play project will automatically bind all of the `AuthInfo` instances to the project's Slick-based implementations, and a Guice provider for a `DelegableAuthInfoRepository` with all of them wired in.  To be clear, this includes instances of the following `AuthInfo` types:


To enable the module, add this line to your `application.conf`:

```
play.modules.enabled += com.ironhorsesoftware.play.silhouette.persistence.SlickPersistenceModule
```

#### Binding Individual DAOs

To instead bind the individual DAOs, import the relevant ones from the `com.ironhorsesoftware.play.silhouette.persistence.daos` package and bind them yourself:

```scala
import javax.inject.Inject
import com.google.inject.{AbstractModule, Provides}
import net.codingwell.scalaguice.ScalaModule
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.impl.providers.{OAuth1Info, OAuth2Info, OpenIDInfo, CasInfo, GoogleTotpInfo}
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.persistence.repositories.DelegableAuthInfoRepository
import com.ironhorsesoftware.play.silhouette.persistence.daos.{SlickPasswordInfoDAO}
import com.ironhorsesoftware.play.silhouette.persistence.daos.SlickCasInfoDAO
import com.ironhorsesoftware.play.silhouette.persistence.daos.{SlickOAuth1InfoDAO, SlickOAuth2InfoDAO}
import com.ironhorsesoftware.play.silhouette.persistence.daos.{SlickOpenIDInfoDAO, SlickGoogleTotpInfoDAO}
import scala.concurrent.ExecutionContext.Implicits.global

class MySlickPersistenceModule @Inject() extends AbstractModule with ScalaModule {

  override def configure {
    // Exclude the bindings you do not need.
    bind[DelegableAuthInfoDAO[CasInfo]].to[SlickCasInfoDAO]
    bind[DelegableAuthInfoDAO[PasswordInfo]].to[SlickPasswordInfoDAO]
    bind[DelegableAuthInfoDAO[OAuth1Info]].to[SlickOAuth1InfoDAO]
    bind[DelegableAuthInfoDAO[OAuth2Info]].to[SlickOAuth2InfoDAO]
    bind[DelegableAuthInfoDAO[OpenIDInfo]].to[SlickOpenIDInfoDAO]
    bind[DelegableAuthInfoDAO[GoogleTotpInfo]].to[SlickGoogleTotpInfoDAO]
  }

  @Provides
  def provideAuthInfoRepository( // Exclude the DAOs you do not need.
      casInfoDAO : DelegableAuthInfoDAO[CasInfo],
      passwordInfoDAO : DelegableAuthInfoDAO[PasswordInfo],
      oauth1InfoDAO : DelegableAuthInfoDAO[OAuth1Info],
      oauth2InfoDAO : DelegableAuthInfoDAO[OAuth2Info],
      openIdInfoDAO : DelegableAuthInfoDAO[OpenIDInfo],
      googleTotpInfoDAO : DelegableAuthInfoDAO[GoogleTotpInfo]) : AuthInfoRepository = {

    // Exclude the DAOs you do not need.
    new DelegableAuthInfoRepository(casInfoDAO, passwordInfoDAO, oauth1InfoDAO, oauth2InfoDAO, openIdInfoDAO, googleTotpInfoDAO)
  }
}
```

### AuthenticatorRepository

In addition to the external authentication providers, Silhouette also provides [authenticators](https://www.silhouette.rocks/docs/authenticator) to validate the user's Play session after login.  There are four implementations of the `StorableAuthenticator` class, which represents authenticators that can be stored:

1. `BearerTokenAuthenticator`
1. `CookieAuthenticator`
1. `JWTAuthenticator`
1. `FakeAuthenticator`

This project provides Slick bindings for the first three.  They can be used as follows, when constructing the relevant authenticator in your Play application:

#### SlickPersistenceModule

#### BearerTokenAuthenticator

#### CookieAuthenticator

#### JWTAuthenticator

## SQL

You will need to add the following tables to your database.


