import SonatypeKeys._
import TypelevelKeys._
import org.typelevel.sbt.Developer
import org.typelevel.sbt.Version._
import sbtrelease.ReleasePlugin.ReleaseKeys._

organization := "com.beamly.flumeback"
name := "flumeback"
lastRelease in ThisBuild := Relative(0, Final)

description := "flumeback: A Logback appender for Flume"
homepage := Some(url("https://github.com/beamly/flumeback"))
startYear := Some(2014)
licenses := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

scalaVersion  := "2.11.4"
crossScalaVersions  := Seq(scalaVersion.value, "2.10.4")
scalacOptions ++= Seq("-encoding", "utf8")
scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlint")
scalacOptions  += "-Xfatal-warnings"
scalacOptions  += "-Xfuture"
scalacOptions  += "-Yinline-warnings"
scalacOptions  += "-Yno-adapted-args"
scalacOptions  += "-Ywarn-dead-code"
scalacOptions  += "-Ywarn-numeric-widen"
scalacOptions ++= CrossVersion partialVersion scalaVersion.value collect {
  case (2, scalaMajor) if scalaMajor > 11 => Seq("-Ywarn-unused-import")
} getOrElse Seq.empty
scalacOptions  += "-Ywarn-value-discard"

libraryDependencies += "ch.qos.logback"          %  "logback-classic"        % "1.1.2"
libraryDependencies += "net.databinder.dispatch" %% "dispatch-core"          % "0.11.1"
libraryDependencies += "net.databinder.dispatch" %% "dispatch-json4s-native" % "0.11.1"
libraryDependencies += "org.specs2"              %% "specs2"                 % "2.4.2" % "test"

typelevelDefaultSettings
typelevelBuildInfoSettings

crossBuild := true

pgpPublicRing := Path.userHome / ".gnupg" / "beamly-pubring.gpg"
pgpSecretRing := Path.userHome / ".gnupg" / "beamly-secring.gpg"

profileName := "com.beamly"

githubProject := ("beamly", "flumeback")
githubDevs := Seq(Developer("Dale Wijnand", "dwijnand"), Developer("Ali Asad Lotia", "lotia"))
apiURL := Some(url("http://beamly.github.io/flumeback/latest/api/"))
autoAPIMappings := true

site.settings
site.includeScaladoc()

ghpages.settings
git.remoteRepo := "git@github.com:beamly/flumeback.git"

watchSources ++= (baseDirectory.value * "*.sbt").get
watchSources ++= (baseDirectory.value / "project" * "*.scala").get
