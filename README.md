# Clock

The `silhouette` project provides an alternate binding for `Clock` which uses the Java `LocalDateTime` instead of the Joda `DateTime`.

# Silhouette + Slick

The `silhouette-persistence` project provides bindings to persist all of the Silhouette `AuthInfo` and `StorableAuthenticator` (excluding `FakeAuthenticator`) objects to a relational database using Slick.

## `build.sbt`

Add the following to your `build.sbt` to include the library as a dependency.

```scala
externalResolvers += "Iron Horse Software Silhouette Packages" at "https://maven.pkg.github.com/ironhorsesoftware/silhouette"
libraryDependencies +=  "silhouette-persistence" %% "silhouette-persistence" % "<version>"
```

Currently, the only version is `0.6.1`, which builds against Silhouette 6.0 / Play 2.7 / Scala 2.12.  More to come.

## Usage

### AuthInfo

Silhouette provides the following classes to store the authentication information for each [Silhouette provider](https://www.silhouette.rocks/docs/providers):

1. `CasInfo`
1. `GoogleTotpInfo`
1. `OAuth1Info`
1. `OAuth2Info`
1. `OpenIDInfo`
1. `PasswordInfo`

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

This project provides Slick bindings for the first three, which are used when constructing the relevant `Authenticator[T]` for each environment.  The following examples are below.

*Note:* The following examples require the `"com.iheart" %% "ficus"` project added to your `build.sbt`, and were tested with version `1.4.3`.

While the code shows examples on how to bind most of the required items to construct the relevant `AuthenticatorService`s, two are missing:

* `authenticator-signer`
* `authenticator-crypter`

The Silhouette project provides examples on how to construct these.

#### BearerTokenAuthenticator

```scala
import play.api.Configuration
import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides}
import net.codingwell.scalaguice.ScalaModule
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.ceedubs.ficus.readers.ValueReader
import net.codingwell.scalaguice.ScalaModule
import com.mohiva.play.silhouette.api.repositories.AuthenticatorRepository
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.util.{FingerprintGenerator, IDGenerator, Clock, PasswordInfo}
import com.mohiva.play.silhouette.impl.authenticators.{BearerTokenAuthenticator, BearerTokenAuthenticatorSettings, BearerTokenAuthenticatorService}
import com.ironhorsesoftware.play.silhouette.persistence.repositories.SlickBearerTokenAuthenticatorRepository
import scala.concurrent.ExecutionContext.Implicits.global

class SlickPersistenceModule @Inject() extends AbstractModule with ScalaModule {

  override def configure {
    bind[IDGenerator].toInstance(new SecureRandomIDGenerator())
    bind[Clock].toInstance(Clock())

    bind[AuthenticatorRepository[BearerTokenAuthenticator]].to[SlickBearerTokenAuthenticatorRepository]
  }

  /**
   * Provides the BearerTokenAuthenticator service.
   *
   * @param idGenerator The ID generator implementation.
   * @param configuration The Play configuration.
   * @param clock The clock instance.
   * @param repository The repository to store JWTAuthenticators with.
   * @return The authenticator service.
   */
  @Provides
  def provideAuthenticatorService(
    idGenerator: IDGenerator,
    configuration: Configuration,
    clock: Clock,
    repository : AuthenticatorRepository[BearerTokenAuthenticator]): AuthenticatorService[BearerTokenAuthenticator] = {

    // Use your mechanism of choice to read the BearerTokenAuthenticatorSettings.
    val config = configuration.underlying.as[BearerTokenAuthenticatorSettings]("silhouette.authenticator.bearer")

    new BearerTokenAuthenticatorService(config, repository, idGenerator, clock)
  }
}
```

#### CookieAuthenticator

```scala
import com.typesafe.config.Config
import play.api.Configuration
import play.api.mvc.{Cookie, CookieHeaderEncoding}
import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides}
import net.codingwell.scalaguice.ScalaModule
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.ceedubs.ficus.readers.EnumerationReader._
import net.ceedubs.ficus.readers.ValueReader
import net.codingwell.scalaguice.ScalaModule
import com.mohiva.play.silhouette.api.repositories.AuthenticatorRepository
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.util.{FingerprintGenerator, IDGenerator, Clock, PasswordInfo}
import com.mohiva.play.silhouette.impl.authenticators.{CookieAuthenticator, CookieAuthenticatorSettings, CookieAuthenticatorService}
import com.ironhorsesoftware.play.silhouette.persistence.repositories.SlickCookieAuthenticatorRepository
import scala.concurrent.ExecutionContext.Implicits.global

class SlickPersistenceModule @Inject() extends AbstractModule with ScalaModule {

  /**
   * This is required to read the CookieAuthenticatorSettings from the Play configuration.
   * Feel free to use your own mechanism for reading the settings.
   *
   * A very nested optional reader, to support these cases:
   * Not set, set None, will use default ('Lax')
   * Set to null, set Some(None), will use 'No Restriction'
   * Set to a string value try to match, Some(Option(string))
   */
  implicit val sameSiteReader: ValueReader[Option[Option[Cookie.SameSite]]] =
    (config: Config, path: String) => {
      if (config.hasPathOrNull(path)) {
        if (config.getIsNull(path))
          Some(None)
        else {
          Some(Cookie.SameSite.parse(config.getString(path)))
        }
      } else {
        None
      }
  }

  override def configure {
    bind[IDGenerator].toInstance(new SecureRandomIDGenerator())
    bind[FingerprintGenerator].toInstance(new DefaultFingerprintGenerator(false))
    bind[Clock].toInstance(Clock())

    bind[AuthenticatorRepository[CookieAuthenticator]].to[SlickCookieAuthenticatorRepository]
  }

  /**
   * Provides the CookieAuthenticator service.
   *
   * @param signer The signer implementation.
   * @param crypter The crypter implementation.
   * @param cookieHeaderEncoding Logic for encoding and decoding `Cookie` and `Set-Cookie` headers.
   * @param fingerprintGenerator The fingerprint generator implementation.
   * @param idGenerator The ID generator implementation.
   * @param configuration The Play configuration.
   * @param clock The clock instance.
   * @param repository The repository to store CookieAuthenticators with.
   * @return The authenticator service.
   */
  @Provides
  def provideAuthenticatorService(
    @Named("authenticator-signer") signer: Signer,
    @Named("authenticator-crypter") crypter: Crypter,
    cookieHeaderEncoding: CookieHeaderEncoding,
    fingerprintGenerator: FingerprintGenerator,
    idGenerator: IDGenerator,
    configuration: Configuration,
    clock: Clock,
    repository : AuthenticatorRepository[CookieAuthenticator]): AuthenticatorService[CookieAuthenticator] = {

    // Use your mechanism of choice to read the CookieAuthenticatorSettings.
    val config = configuration.underlying.as[CookieAuthenticatorSettings]("silhouette.authenticator.cookie")
    val authenticatorEncoder = new CrypterAuthenticatorEncoder(crypter)

    new CookieAuthenticatorService(config, Some(repository), signer, cookieHeaderEncoding, authenticatorEncoder, fingerprintGenerator, idGenerator, clock)
  }
}
```

#### JWTAuthenticator

```scala
import play.api.Configuration
import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides}
import net.codingwell.scalaguice.ScalaModule
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.ceedubs.ficus.readers.EnumerationReader._
import net.ceedubs.ficus.readers.ValueReader
import net.codingwell.scalaguice.ScalaModule
import com.mohiva.play.silhouette.api.repositories.AuthenticatorRepository
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.util.{FingerprintGenerator, IDGenerator, Clock, PasswordInfo}
import com.mohiva.play.silhouette.impl.authenticators.{JWTAuthenticator, JWTAuthenticatorSettings, JWTAuthenticatorService}
import com.ironhorsesoftware.play.silhouette.persistence.repositories.SlickJWTAuthenticatorRepository
import scala.concurrent.ExecutionContext.Implicits.global

class SlickPersistenceModule @Inject() extends AbstractModule with ScalaModule {

  override def configure {
    bind[IDGenerator].toInstance(new SecureRandomIDGenerator())
    bind[Clock].toInstance(Clock())

    bind[AuthenticatorRepository[JWTAuthenticator]].to[SlickJWTAuthenticatorRepository]
  }

  /**
   * Provides the JWTAuthenticator service.
   *
   * @param crypter The crypter implementation.
   * @param idGenerator The ID generator implementation.
   * @param configuration The Play configuration.
   * @param clock The clock instance.
   * @param repository The repository to store JWTAuthenticators with.
   * @return The authenticator service.
   */
  @Provides
  def provideAuthenticatorService(
    @Named("authenticator-crypter") crypter: Crypter,
    idGenerator: IDGenerator,
    configuration: Configuration,
    clock: Clock,
    repository : AuthenticatorRepository[JWTAuthenticator]): AuthenticatorService[JWTAuthenticator] = {

    // Use your mechanism of choice to read the JWTAuthenticatorSettings
    val config = configuration.underlying.as[JWTAuthenticatorSettings]("silhouette.authenticator.jwt")
    val authenticatorEncoder = new CrypterAuthenticatorEncoder(crypter)

    new JWTAuthenticatorService(config, Some(repository), authenticatorEncoder, idGenerator, clock)
  }
}
```

### Database Schema

You will need to add tables to your database depending on which DAOs and Repositories you use.  The database type information is provided below, with some examples for PostgreSQL.

For testing purposes, each of the DAOs and Repositories come with two methods: `createSchema` and `dropSchema`, which will use Slick to create and drop the tables.

#### `SlickCasInfoDAO`

##### `credentials_cas` Table Definition

Field Name  |Scala Type|Nullable?|Notes
------------|----------|---------|-----
id          |Int       |No       |Primary Key + Auto-Increment
provider_id |String    |No       |This is the Provider ID in the LoginInfo. Consider indexing this field.
provider_key|String    |No       |This is the Provider Key in the LoginInfo.  Consider indexing this field.
ticket      |String    |No       |This is the CAS Ticket.

#### `SlickGoogleTotpInfoDAO`

##### `credentials_totp_google` Table Definition

Field Name  |Scala Type|Nullable?|Notes
------------|----------|---------|-----
id          |Int       |No       |Primary Key + Auto-Increment
provider_id |String    |No       |This is the Provider ID in the LoginInfo. Consider indexing this field.
provider_key|String    |No       |This is the Provider Key in the LoginInfo.  Consider indexing this field.
shared_key  |String    |No       |

##### `credentials_totp_google_scratch_codes` Table Definition

Field Name     |Scala Type|Nullable?|Notes
---------------|----------|---------|-----
id             |Int       |No       |Primary Key + Auto-Increment
google_totp_id |Int       |No       |Foreign Key to `credentials_totp_google`
hasher         |String    |No       |
password       |String    |No       |
salt           |String    |Yes      |

#### `SlickOAuth1InfoDAO`

##### `credentials_oauth1` Table Definition

Field Name  |Scala Type|Nullable?|Notes
------------|----------|---------|-----
id          |Int       |No       |Primary Key + Auto-Increment
provider_id |String    |No       |This is the Provider ID in the LoginInfo. Consider indexing this field.
provider_key|String    |No       |This is the Provider Key in the LoginInfo.  Consider indexing this field.
token       |String    |No       |
secret      |String    |No       |

##### PostgreSQL Example

```sql
CREATE TABLE credentials_oauth1 (
    id           SERIAL PRIMARY KEY,
    provider_id  TEXT NOT NULL,
    provider_key TEXT NOT NULL,
    token        TEXT NOT NULL,
    secret       TEXT NOT NULL
);
```

#### `SlickOAuth2InfoDAO`

##### `credentials_oauth2` Table Definition

Field Name   |Scala Type|Nullable?|Notes
-------------|----------|---------|-----
id           |Int       |No       |Primary Key + Auto-Increment
provider_id  |String    |No       |This is the Provider ID in the LoginInfo. Consider indexing this field.
provider_key |String    |No       |This is the Provider Key in the LoginInfo.  Consider indexing this field.
access_token |String    |No       |
token_type   |String    |Yes      |
expires_in   |Int       |Yes      |
refresh_token|String    |Yes      |
params       |String    |Yes      |The Map[String, String] params are stored as a JSON object.

##### PostgreSQL Example

```sql
CREATE TABLE credentials_oauth2 (
    id              SERIAL    PRIMARY KEY,
    provider_id     TEXT                   NOT NULL,
    provider_key    TEXT                   NOT NULL,
    access_token    TEXT                   NOT NULL,
    token_type      TEXT,
    expires_in      INTEGER,
    refresh_token   TEXT,
    params          TEXT
);
```

#### `SlickOpenIDInfoDAO`

##### `credentials_openid` Table Definition

Field Name   |Scala Type|Nullable?|Notes
-------------|----------|---------|-----
id           |Int       |No       |Primary Key + Auto-Increment
provider_id  |String    |No       |This is the Provider ID in the LoginInfo. Consider indexing this field.
provider_key |String    |No       |This is the Provider Key in the LoginInfo.  Consider indexing this field.
openid       |String    |No       |
attributes   |String    |No       |The attributes are stored as a JSON object.

##### PostgreSQL Example

```sql
CREATE TABLE credentials_openid (
    id           SERIAL PRIMARY KEY,
    provider_id  TEXT NOT NULL,
    provider_key TEXT NOT NULL,
    openid TEXT  NOT NULL,
    attributes   TEXT NOT NULL
);
```

#### `SlickPasswordInfoDAO`

##### `credentials_password` Table Definition

Field Name      |Scala Type|Nullable?|Notes
----------------|----------|---------|-----
id              |Int       |No       |Primary Key + Auto-Increment
provider_id     |String    |No       |This is the Provider ID in the LoginInfo. Consider indexing this field.
provider_key    |String    |No       |This is the Provider Key in the LoginInfo.  Consider indexing this field.
password        |String    |No       |
password_hasher |String    |No       |
password_salt   |String    |Yes      |

##### PostgreSQL Example

```sql
CREATE TABLE credentials_password (
    id              SERIAL    PRIMARY KEY,
    provider_id     TEXT                   NOT NULL,
    provider_key    TEXT                   NOT NULL,
    password        TEXT                   NOT NULL,
    password_hasher TEXT                   NOT NULL,
    password_salt   TEXT
);
```

#### `SlickBearerTokenAuthenticatorRepository`

##### `authentication_bearer_tokens` Table Definition

Field Name      |Scala Type|Nullable?|Notes
----------------|----------|---------|-----
id              |Int       |No       |Primary Key + Auto-Increment
authenticator_id|String    |No       |This is the key Silhouette will use.  Consider indexing this field.
provider_id     |String    |No       |
provider_key    |String    |No       |
last_used_at    |Timestamp |No       |The timestamp will be recorded in UTC.
expires_at      |Timestamp |No       |The timestamp will be recorded in UTC.
idle_timeout    |Long      |Yes      |The idle timeout will be recorded in milliseconds.

#### `SlickCookieAuthenticatorRepository`

##### `authentication_cookies` Table Definition

Field Name      |Scala Type|Nullable?|Notes
----------------|----------|---------|-----
id              |Int       |No       |Primary Key + Auto-Increment
authenticator_id|String    |No       |This is the key Silhouette will use.  Consider indexing this field.
provider_id     |String    |No       |
provider_key    |String    |No       |
last_used_at    |Timestamp |No       |The timestamp will be recorded in UTC.
expires_at      |Timestamp |No       |The timestamp will be recorded in UTC.
idle_timeout    |Long      |Yes      |The idle timeout will be recorded in milliseconds.
max_age         |Long      |Yes      |The maximum age will be recorded in milliseconds.
fingerprint     |String    |Yes      |

##### PostgreSQL Example

```sql
CREATE TABLE authentication_cookies (
    id               SERIAL PRIMARY KEY,
    authenticator_id TEXT NOT NULL,
    provider_id      TEXT NOT NULL,
    provider_key     TEXT NOT NULL,
    last_used_at     TIMESTAMP NOT NULL,
    expires_at       TIMESTAMP NOT NULL,
    idle_timeout     BIGINT,
    max_age          BIGINT,
    fingerprint      TEXT
);
```

#### `SlickJWTAuthenticatorRepository`

##### `authentication_jwts` Table Definition

Field Name      |Scala Type|Nullable?|Notes
----------------|----------|---------|-----
id              |Int       |No       |Primary Key + Auto-Increment
authenticator_id|String    |No       |This is the key Silhouette will use.  Consider indexing this field.
provider_id     |String    |No       |
provider_key    |String    |No       |
last_used_at    |Timestamp |No       |The timestamp will be recorded in UTC.
expires_at      |Timestamp |No       |The timestamp will be recorded in UTC.
idle_timeout    |Long      |Yes      |The idle timeout will be recorded in milliseconds.
custom_claims   |String    |Yes      |The custom claims will be recorded as a JSON object.
