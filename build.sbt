import Settings._

// Global Settings
ThisBuild / scalaVersion    := "2.13.8"
ThisBuild / organization    := "Prom3theus"
ThisBuild / versionScheme   := Some("early-semver")
ThisBuild / dynverSeparator := "-"
ThisBuild / conflictManager := ConflictManager.latestRevision
ThisBuild / javacOptions ++= Seq("-source", "17", "-target", "17")
ThisBuild / scalacOptions ++= Seq("-Ymacro-annotations", "-target:jvm-17")

lazy val root = (project in file("."))
  .settings(
    name := "zio-actors-shardcake"
  )
  .settings(CommandAliases.aliases)
  .aggregate(commons, `shard-manager`, zio)
  .dependsOn(commons, `shard-manager`, zio)
  .enablePlugins(JavaAppPackaging, SbtNativePackager)

lazy val commons = project
  .settings(
    name := "commons"
  )
  .settings(commonSettings, scalafixSettings)
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.Zio.all,
      Dependencies.Shardcake.all
    ).flatten
  )
  .enablePlugins(ScalafixPlugin, JavaAppPackaging, SbtNativePackager)

val commonsDep = commons % "compile->compile;test->test"

lazy val `shard-manager` = project
  .settings(
    name := "shard-manager"
  )
  .settings(commonSettings, scalafixSettings)
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.Zio.all,
      Dependencies.Shardcake.all
    ).flatten
  )
  .enablePlugins(ScalafixPlugin)
  .dependsOn(commonsDep)
  .settings(
    dockerSettings,
    Docker / packageName        := "prom3theus/shard-manager",
    Docker / dockerExposedPorts := Seq(54321, 8080)
  )
  .enablePlugins(DockerPlugin, AshScriptPlugin, JavaAppPackaging, SbtNativePackager)

lazy val zio = project
  .settings(
    name := "zio"
  )
  .settings(commonSettings, scalafixSettings)
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.Zio.all,
      Dependencies.Shardcake.all
    ).flatten
  )
  .enablePlugins(ScalafixPlugin)
  .dependsOn(commonsDep)
  .settings(
    dockerSettings,
    Docker / packageName        := "prom3theus/zio-actors-shardcake",
    Docker / dockerExposedPorts := Seq(54321)
  )
  .enablePlugins(DockerPlugin, AshScriptPlugin, JavaAppPackaging, SbtNativePackager)
