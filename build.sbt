import com.softwaremill.SbtSoftwareMillCommon.commonSmlBuildSettings
import com.softwaremill.Publish.ossPublishSettings

val scala212 = "2.12.15"
val scala213 = "2.13.8"
val scala3 = "3.1.2"

lazy val supportedScalaVersions = List(scala212, scala213, scala3)

lazy val commonSettings = commonSmlBuildSettings ++ ossPublishSettings ++ Seq(
  organization := "com.softwaremill.common"
)

val scalaTest = "org.scalatest" %% "scalatest" % "3.2.12" % Test
val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5"

val fastUuid = Seq(
  "com.eatthepath" % "fast-uuid" % "0.2.0",
  "com.fasterxml.uuid" % "java-uuid-generator" % "4.0.1"
).map(_ % Test)

lazy val rootProject = (project in file("."))
  .settings(commonSettings: _*)
  .settings(publish / skip := true, name := "id-generator-root", scalaVersion := scala213)
  .aggregate(core.projectRefs: _*)

lazy val core = (projectMatrix in file("core"))
  .settings(commonSettings: _*)
  .settings(
    name := "id-generator"
  )
  .jvmPlatform(
    scalaVersions = supportedScalaVersions,
    libraryDependencies ++= Seq(
      scalaTest,
      scalaLogging
    ) ++ fastUuid
  )
