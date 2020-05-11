lazy val scala213 = "2.13.0"
lazy val scala212 = "2.12.8"
lazy val scala211 = "2.11.12"

ThisBuild / scalaVersion := scala212

ThisBuild / organization := "com.ironhorsesoftware.silhouette"
ThisBuild / organizationName := "Iron Horse Software, L.L.C."
ThisBuild / organizationHomepage := Some(url("http://ironhorsesoftware.com/"))

lazy val projectMaintainer = "mpigott@ironhorsesoftware.com"

lazy val silhouetteVersion = "6.1.1"
lazy val playVersion = "2.7.3"
lazy val playSlickVersion = "4.0.2"
lazy val scalaGuiceVersion = "4.2.6"
lazy val scalaTestVersion = "3.1.1"

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/ironhorsesoftware/silhouette"),
    "scm:git@github.com:ironhorsesoftware/silhouette.git"
  )
)

ThisBuild / developers := List(
  Developer(
    id    = "mpigott",
    name  = "Michael Pigott",
    email = "mpigott@ironhorsesoftware.com",
    url   = url("http://ironhorsesoftware.com")
  )
)

ThisBuild / homepage := Some(url("https://github.com/ironhorsesoftware/silhouette"))

ThisBuild / licenses := List("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))

ThisBuild / pomIncludeRepository := { _ => false }

lazy val root = (project in file ("."))
	.aggregate(silhouette, silhouettePersistence)
	.settings(
		name := "root",
		version := "0.0.3-SNAPSHOT",
		crossScalaVersions := Nil,
		scalacOptions ++= Seq("-deprecation", "-unchecked", "-language:_"),
		publishLocal := {},
		publishM2 := {},
		publish / skip := true
	).dependsOn(silhouette, silhouettePersistence)

lazy val silhouette = (project in file("silhouette"))
	.enablePlugins(JavaAppPackaging)
	.settings(
		name := "silhouette",
		version := "0.0.3-SNAPSHOT",
		scalacOptions ++= Seq("-deprecation", "-unchecked", "-language:_"),
		crossScalaVersions := Seq(scala213,scala212),
		maintainer := projectMaintainer,
		publish / skip := true,
		libraryDependencies ++= Seq(
		  "com.typesafe.play"      %% "play-ws"                         % playVersion,
		  "com.typesafe.play"      %% "play-openid"                     % playVersion,
		  "com.mohiva"             %% "play-silhouette"                 % silhouetteVersion,
		  "net.codingwell"         %% "scala-guice"                     % scalaGuiceVersion
		)
	)

lazy val silhouettePersistence = (project in file ("silhouette-persistence"))
	.enablePlugins(JavaAppPackaging)
	.settings(
		name := "silhouette-persistence",
		version := "0.6.1",
		scalacOptions ++= Seq("-deprecation", "-unchecked", "-language:_"),
		crossScalaVersions := Seq(scala211,scala212,scala213),
		maintainer := projectMaintainer,
		description := "A set of Slick-based DAOs and Repositories for Silhouette.",
		publishTo := {
		  val nexus = "https://oss.sonatype.org/"
		  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
		  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
		},
		publishMavenStyle := true,
		libraryDependencies ++= Seq(
		    "com.mohiva"             %% "play-silhouette"               % silhouetteVersion,
			"com.mohiva"             %% "play-silhouette-cas"           % silhouetteVersion,
			"com.mohiva"             %% "play-silhouette-persistence"   % silhouetteVersion,
			"com.mohiva"             %% "play-silhouette-totp"          % silhouetteVersion,
			"com.typesafe.play"      %% "play-slick"                    % playSlickVersion,
		    "net.codingwell"         %% "scala-guice"                   % scalaGuiceVersion,
			"com.h2database"          % "h2"                            % "1.4.200"        % Test,
			"com.typesafe.play"      %% "play-specs2"                   % playVersion      % Test
		)
	)
