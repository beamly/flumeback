import sbt.Path.userHome
import SbtKitPre._

import SonatypeKeys._
import TypelevelKeys._
import org.typelevel.sbt.Developer
import sbtrelease.ReleasePlugin.ReleaseKeys._

val repoUser = "beamly"
val repoProj = "flumeback"

val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.1.3"
val logbackAccess  = "ch.qos.logback" % "logback-access"  % "1.1.3"

val commonSettings: Seq[Setting[_]] = Settings(
  organization := "com.beamly.flumeback",

  description := "A Logback appender for Flume",
  homepage := Some(url(s"https://github.com/$repoUser/$repoProj")),
  startYear := Some(2014),
  licenses := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),

  scalaVersion := "2.11.7",
  crossScalaVersions := Seq(scalaVersion.value, "2.10.5"),

  scalacOptions ++= Seq("-encoding", "utf8"),
  scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlint"),
  scalacOptions  += "-language:higherKinds",
  scalacOptions  += "-language:implicitConversions",
  scalacOptions  += "-language:postfixOps",
  scalacOptions  += "-Xfatal-warnings",
  scalacOptions  += "-Xfuture",
  scalacOptions  += "-Yinline-warnings",
  scalacOptions  += "-Yno-adapted-args",
  scalacOptions  += "-Ywarn-dead-code",
  scalacOptions  += "-Ywarn-numeric-widen",
  scalacOptions ++= "-Ywarn-unused-import".ifScala211Plus.value.toSeq,
  scalacOptions  += "-Ywarn-value-discard",

  libraryDependencies += logbackClassic,
  libraryDependencies += "org.json4s" %% "json4s-core"   % "3.2.9",
  libraryDependencies += "org.json4s" %% "json4s-native" % "3.2.9",
  libraryDependencies += "org.specs2" %% "specs2"        % "2.4.2"  % "test",

  typelevelDefaultSettings,
  typelevelBuildInfoSettings,
  buildInfoPackage := name.value.map(ch => if (ch == '-') '.' else ch),

  crossBuild := true,

  pgpPublicRing := userHome / ".gnupg" / "beamly-pubring.gpg",
  pgpSecretRing := userHome / ".gnupg" / "beamly-secring.gpg",

  profileName := "com.beamly",

  githubProject := (repoUser, repoProj),
  githubDevs := Seq(Developer("Dale Wijnand", "dwijnand"), Developer("Ali Asad Lotia", "lotia")),
  apiURL := Some(url(s"http://$repoUser.github.io/$repoProj/latest/api/")),
  autoAPIMappings := true,

  site.settings,
  site.includeScaladoc(),

  ghpages.settings,
  git.remoteRepo := s"git@github.com:$repoUser/$repoProj.git"
)

lazy val flumebackRoot = (project in file(".") settings (commonSettings: _*) settings (noArtifacts: _*)
  dependsOn (`flumeback-core`, flumeback, `flumeback-access`)
  aggregate (`flumeback-core`, flumeback, `flumeback-access`)
)

val `flumeback-core` = project settings (commonSettings: _*)

val flumeback = project dependsOn `flumeback-core` settings (commonSettings: _*)

val `flumeback-access` = project dependsOn `flumeback-core` settings (commonSettings: _*) settings(
  libraryDependencies += logbackAccess,
  libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided"
)

def Settings(settings: SettingsDefinition*): Seq[Setting[_]] = settings.flatMap(_.settings)

val noPackage = Settings(Keys.`package` := file(""), packageBin := file(""), packagedArtifacts := Map())
val noPublish = Settings(publish := {}, publishLocal := {}, publishArtifact := false)
val noArtifacts = Settings(noPackage, noPublish)

watchSources ++= (baseDirectory.value * "*.sbt").get
watchSources ++= (baseDirectory.value / "project" * "*.scala").get
