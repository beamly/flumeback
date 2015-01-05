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
  val repoUser = "beamly"
  val repoProj = "flumeback"

  val flumeback = project in file(".") smartSettings (
    organization := "com.beamly.flumeback",
    name := "flumeback",

    description := "A Logback appender for Flume",
    homepage := Some(url(s"https://github.com/$repoUser/$repoProj")),
    startYear := Some(2014),
    licenses := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),

    scalaVersion := "2.11.4",
    crossScalaVersions := Seq(scalaVersion.value, "2.10.4"),
    scalacOptions ++= Seq("-encoding", "utf8"),
    scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlint"),
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
    watchSources ++= (baseDirectory.value / "project" * "*.scala").get)

  def scalaPartV = Def setting (CrossVersion partialVersion scalaVersion.value)

  implicit final class AnyWithIfScala11Plus[A](val _o: A) {
    def ifScala211Plus = Def setting (scalaPartV.value collect { case (2, y) if y >= 11 => _o })
  }

  implicit final class ProjectWithAlso(val _p: Project) {
    def also(ss: Seq[Setting[_]]) = _p settings (ss.toSeq: _*)

    def smartSettings(sls: SettingLine*) = _p also SmartSettings(sls: _*)
  }

  implicit final class SettingKeyWithRemove[A](val _sk: SettingKey[Seq[A]]) {
    def ~-=(p: A => Boolean): Setting[Seq[A]] = _sk ~= (_ filterNot p)
    def -=(a: A): Setting[Seq[A]] = _sk ~= (_ filterNot a.==)
  }
  implicit final class DefinableTaskWithRemove[A](val _sk: DefinableTask[Seq[A]]) {
    def ~-=(p: A => Boolean): Setting[Task[Seq[A]]] = _sk ~= (_ filterNot p)
    def -=(a: A): Setting[Task[Seq[A]]] = _sk ~= (_ filterNot a.==)
  }

  def SmartSettings(sls: SettingLine*): Seq[Setting[_]] = sls flatMap (_.ss)

  sealed trait SettingLine {def ss: Seq[Setting[_]] }
  implicit final class SingleSetting(val s: Setting[_]) extends SettingLine {val ss = Seq[Setting[_]](s) }
  implicit final class MultiSettings(val ss: Seq[Setting[_]]) extends SettingLine
}
