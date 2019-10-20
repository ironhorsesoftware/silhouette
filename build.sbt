lazy val scala213 = "2.13.0"
lazy val scala212 = "2.12.8"
lazy val scala211 = "2.11.12"

scalaVersion := scala212

organization := "com.ironhorsesoftware"

lazy val silhouetteVersion = "6.1.1"
lazy val playVersion = "2.7.3"
lazy val slickVersion = ""

lazy val root = (project in file ("."))
	.aggregate(silhouette, silhouettePersistence)
	.settings(
		name := "root",
		crossScalaVersions := Seq(scala213, scala212, scala211),
	)

lazy val silhouette = (project in file("silhouette")).settings(
	name := "silhouette",
	libraryDependencies ++= Seq(
	  "com.typesafe.play"      %% "play-ws"                         % playVersion,
	  "com.typesafe.play"      %% "play-openid"                     % playVersion,
      "com.mohiva"             %% "play-silhouette"                 % silhouetteVersion,
      "net.codingwell"         %% "scala-guice"                     % "4.2.5"
	)
)

lazy val silhouettePersistence = (project in file ("silhouette-persistence"))
	.dependsOn(silhouette)
	.settings(
	name := "silhouette-persistence",
	libraryDependencies ++= Seq(
		"com.mohiva"             %% "play-silhouette-persistence"   % silhouetteVersion,
		"com.typesafe.slick"     %% "slick"                         % "3.3.2"
	)
)
