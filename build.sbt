lazy val commonSettings = commonSmlBuildSettings ++ ossPublishSettings ++ Seq(
  organization := "com.softwaremill.common",
  scalaVersion := "2.12.6"
)

val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5" % "test"
val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"

lazy val rootProject = (project in file("."))
  .settings(commonSettings: _*)
  .settings(
    name := "id-generator",
    libraryDependencies ++= Seq(
      scalaTest, scalaLogging
    )
  )

