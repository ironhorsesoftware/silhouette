lazy val scala213 = "2.13.0"
lazy val scala212 = "2.12.8"
lazy val scala211 = "2.11.12"

scalaVersion := scala212

organization := "com.ironhorsesoftware"
lazy val projectMaintainer = "mpigott@ironhorsesoftware.com"

lazy val silhouetteVersion = "6.1.1"
lazy val playVersion = "2.7.3"
lazy val playSlickVersion = "4.0.2"

lazy val root = (project in file ("."))
	.aggregate(silhouette, silhouettePersistence)
	.settings(
		name := "root",
		crossScalaVersions := Seq(scala213, scala212, scala211),
		publishLocal := {},
		publishM2 := {},
		publishArtifact := false
	).dependsOn(silhouette, silhouettePersistence)

lazy val silhouette = (project in file("silhouette"))
	.enablePlugins(JavaAppPackaging)
	.settings(
		name := "silhouette",
		maintainer := projectMaintainer,
		libraryDependencies ++= Seq(
		  "com.typesafe.play"      %% "play-ws"                         % playVersion,
		  "com.typesafe.play"      %% "play-openid"                     % playVersion,
		  "com.mohiva"             %% "play-silhouette"                 % silhouetteVersion,
		  "net.codingwell"         %% "scala-guice"                     % "4.2.6"
		)
	)

lazy val silhouettePersistence = (project in file ("silhouette-persistence"))
	.dependsOn(silhouette)
	.enablePlugins(JavaAppPackaging)
	.settings(
		name := "silhouette-persistence",
		maintainer := projectMaintainer,
		libraryDependencies ++= Seq(
			"com.mohiva"             %% "play-silhouette-cas"           % silhouetteVersion,
			"com.mohiva"             %% "play-silhouette-persistence"   % silhouetteVersion,
			"com.typesafe.play"      %% "play-slick"                    % playSlickVersion
		)
	)
