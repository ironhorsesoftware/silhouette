lazy val scala213 = "2.13.0"
lazy val scala212 = "2.12.8"
lazy val scala211 = "2.11.12"

scalaVersion := scala212

organization := "com.ironhorsesoftware"

lazy val projectMaintainer = "mpigott@ironhorsesoftware.com"
lazy val projectGithubOwner = "ironhorsesoftware"
lazy val projectGithubRepository = "silhouette"

lazy val silhouetteVersion = "6.1.1"
lazy val playVersion = "2.7.3"
lazy val playSlickVersion = "4.0.2"
lazy val scalaGuiceVersion = "4.2.6"
lazy val scalaTestVersion = "3.1.1"

lazy val root = (project in file ("."))
	.aggregate(silhouette, silhouettePersistence)
	.settings(
		name := "root",
		version := "0.0.1-SNAPSHOT",
		crossScalaVersions := Seq(scala213,scala212,scala211),
		scalacOptions ++= Seq("-deprecation", "-unchecked", "-language:_"),
		publishLocal := {},
		publishM2 := {},
		publishArtifact := false
	).dependsOn(silhouette, silhouettePersistence)

lazy val silhouette = (project in file("silhouette"))
	.enablePlugins(JavaAppPackaging)
	.settings(
		name := "silhouette",
		version := "0.0.1-SNAPSHOT",
		scalacOptions ++= Seq("-deprecation", "-unchecked", "-language:_"),
		maintainer := projectMaintainer,
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
		version := "0.6.0-SNAPSHOT",
		scalacOptions ++= Seq("-deprecation", "-unchecked", "-language:_"),
		maintainer := projectMaintainer,
		githubOwner := projectGithubOwner,
		githubRepository := projectGithubRepository,
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
