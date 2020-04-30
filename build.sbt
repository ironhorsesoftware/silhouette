lazy val scala213 = "2.13.0"
lazy val scala212 = "2.12.8"
lazy val scala211 = "2.11.12"

scalaVersion := scala212

organization := "com.ironhorsesoftware"
lazy val projectMaintainer = "mpigott@ironhorsesoftware.com"

lazy val silhouetteVersion = "6.1.1"
lazy val playVersion = "2.7.3"
lazy val playSlickVersion = "4.0.2"
lazy val scalaGuiceVersion = "4.2.6"
lazy val scalaTestVersion = "3.1.1"

lazy val root = (project in file ("."))
	.aggregate(silhouette, silhouettePersistence)
	.settings(
		name := "root",
		crossScalaVersions := Seq(scala213, scala212, scala211),
		scalacOptions ++= Seq("-deprecation", "-unchecked", "-language:_"),
		publishLocal := {},
		publishM2 := {},
		publishArtifact := false
	).dependsOn(silhouette, silhouettePersistence)

lazy val silhouette = (project in file("silhouette"))
	.enablePlugins(JavaAppPackaging)
	.settings(
		name := "silhouette",
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
		scalacOptions ++= Seq("-deprecation", "-unchecked", "-language:_"),
		maintainer := projectMaintainer,
		libraryDependencies ++= Seq(
		    "com.mohiva"           %% "play-silhouette"               % silhouetteVersion,
			"com.mohiva"           %% "play-silhouette-cas"           % silhouetteVersion,
			"com.mohiva"           %% "play-silhouette-persistence"   % silhouetteVersion,
			"com.mohiva"           %% "play-silhouette-totp"          % silhouetteVersion,
			"com.typesafe.play"    %% "play-slick"                    % playSlickVersion,
		    "net.codingwell"       %% "scala-guice"                   % scalaGuiceVersion,
			"org.scalactic"        %% "scalactic"                     % scalaTestVersion,
			"org.scalatest"        %% "scalatest"                     % scalaTestVersion % "test",
			"com.h2database"        % "h2"                            % "1.4.200"        % "test"
		)
	)
