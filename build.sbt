lazy val commonSettings = commonSmlBuildSettings ++ ossPublishSettings ++ Seq(
  organization := "com.softwaremill.common",
  scalaVersion := "2.12.6"
)

val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5" % "test"
val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"

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

