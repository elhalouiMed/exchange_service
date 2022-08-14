name := """exchange_service"""
organization := "formedix"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.8"

libraryDependencies ++= Seq(
  guice,
  ehcache,
  "com.github.tototoshi" %% "scala-csv" % "1.3.10",
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
)

routesGenerator := InjectedRoutesGenerator
// Adds additional packages into Twirl
//TwirlKeys.templateImports += "formedix.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "formedix.binders._"
