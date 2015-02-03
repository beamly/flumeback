import sbt._, Keys._, Path.userHome, Scoped.DefinableTask

import com.typesafe.sbt.SbtGhPages._
import com.typesafe.sbt.SbtGit._
import com.typesafe.sbt.SbtPgp._
import com.typesafe.sbt.SbtSite._
import org.typelevel.sbt.TypelevelPlugin._
import xerial.sbt.Sonatype._

import SonatypeKeys._
import TypelevelKeys._
import org.typelevel.sbt.Developer
import sbtrelease.ReleasePlugin.ReleaseKeys._

import scala.language.implicitConversions

object Build extends Build {
  def scalaPartV = Def setting (CrossVersion partialVersion scalaVersion.value)

  implicit final class AnyWithIfScala11Plus[A](val _o: A) {
    def ifScala211Plus = Def setting (scalaPartV.value collect { case (2, y) if y >= 11 => _o })
  }

  implicit final class ProjectWithAlso(val _p: Project) {
    def also(ss: Seq[Setting[_]]) = _p settings (ss.toSeq: _*)

    def smartSettings(sds: SettingsDefinition*) = _p also SmartSettings(sds: _*)
  }

  implicit final class SettingKeyWithRemove[A](val _sk: SettingKey[Seq[A]]) {
    def -=[E](e: E)(implicit r: Removable[A, E]): Setting[Seq[A]] = _sk ~= r(e)
  }
  implicit final class DefinableTaskWithRemove[A](val _sk: DefinableTask[Seq[A]]) {
    def -=[E](e: E)(implicit r: Removable[A, E]): Setting[Task[Seq[A]]] = _sk ~= r(e)
  }

  def SmartSettings(sds: SettingsDefinition*): Seq[Setting[_]] = sds flatMap (_.settings)

  sealed trait Removable[T, E] extends (E => Seq[T] => Seq[T])
  implicit def RemovableElem[T] = new Removable[T, T] {
    def apply(o: T): Seq[T] => Seq[T] = _ filterNot o.==
  }
  implicit def RemovablePred[T, E <: T => Boolean] = new Removable[T, E] {
    def apply(p: E): Seq[T] => Seq[T] = _ filterNot p
  }

  val repoUser = "beamly"
  val repoProj = "flumeback"

  val commonSettings = Seq[Setting[_]](
    organization := "com.beamly.flumeback",

    scalaVersion := "2.11.5",
    crossScalaVersions := Seq(scalaVersion.value),

    scalacOptions ++= Seq("-encoding", "utf8"),
    scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlint")
  )

  val noArtifacts = Seq[Setting[_]](
    publish := (()),
    publishLocal := (()),
    publishArtifact := false, // TODO: Not necessary?
    Keys.`package` := file(""),
    packageBin := file(""),
    packagedArtifacts := Map()
  )

  lazy val `flumeback-proj` = project in file(".") smartSettings (
    commonSettings,
    noArtifacts
  ) aggregate (flumeback, `flumeback-scratch`)

  val flumeback = project smartSettings (
    commonSettings,

    description := "A Logback appender for Flume",
    homepage := Some(url(s"https://github.com/$repoUser/$repoProj")),
    startYear := Some(2014),
    licenses := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),

    crossScalaVersions := Seq(scalaVersion.value, "2.10.4"),

    scalacOptions  += "-Xfatal-warnings",
    scalacOptions  += "-Xfuture",
    scalacOptions  += "-Yinline-warnings",
    scalacOptions  += "-Yno-adapted-args",
    scalacOptions  += "-Ywarn-dead-code",
    scalacOptions  += "-Ywarn-numeric-widen",
    scalacOptions ++= "-Ywarn-unused-import".ifScala211Plus.value.toSeq,
    scalacOptions  += "-Ywarn-value-discard",

    libraryDependencies += "ch.qos.logback"           % "logback-classic"        % "1.1.2",
    libraryDependencies += "net.databinder.dispatch" %% "dispatch-core"          % "0.11.1",
    libraryDependencies += "net.databinder.dispatch" %% "dispatch-json4s-native" % "0.11.1",
    libraryDependencies += "org.specs2"              %% "specs2"                 % "2.4.2"   % "test",

    typelevelDefaultSettings,
    typelevelBuildInfoSettings,

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
    git.remoteRepo := s"git@github.com:$repoUser/$repoProj.git",

    watchSources ++= (baseDirectory.value * "*.sbt").get,
    watchSources ++= (baseDirectory.value / "project" * "*.scala").get
  )

  val `flumeback-scratch` = (project
    enablePlugins play.PlayScala
    also commonSettings
    dependsOn flumeback
    also noArtifacts
    smartSettings (
      libraryDependencies += "com.beamly.playpen" %% "playpen" % "0.1.0"
    )
  )
}
