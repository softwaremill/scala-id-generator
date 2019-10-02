import com.softwaremill.PublishTravis.publishTravisSettings

lazy val scala212 = "2.12.10"
lazy val scala213 = "2.13.1"

lazy val supportedScalaVersions = List(scala212, scala213)

lazy val commonSettings = commonSmlBuildSettings ++ ossPublishSettings ++ Seq(
  organization := "com.softwaremill.common",
  crossScalaVersions := supportedScalaVersions
)

val scalaTest = "org.scalatest" %% "scalatest" % "3.0.8" % "test"
val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"

val fastUuid = Seq(
  "com.eatthepath" % "fast-uuid" % "0.1",
  "com.fasterxml.uuid" % "java-uuid-generator" % "3.1.5"
).map(_ % Test)

lazy val rootProject = (project in file("."))
  .settings(commonSettings: _*)
  .settings(
    name := "id-generator",
    libraryDependencies ++= Seq(
      scalaTest, scalaLogging
    )  ++ fastUuid
  )
  .settings(publishTravisSettings)


