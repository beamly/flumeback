import SonatypeKeys._
import TypelevelKeys._
import org.typelevel.sbt.Developer
import sbtrelease.ReleasePlugin.ReleaseKeys._

lazy val root = (
  project in file(".") settings (
           organization  := "com.beamly.flumeback",
                   name  := "flumeback",
               homepage  := Some(url("https://github.com/beamly/flumeback")),
              startYear  := Some(2014),
               licenses  := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
           scalaVersion  := "2.11.2",
     crossScalaVersions  := Seq(scalaVersion.value, "2.10.4"),
          scalacOptions ++= Seq("-encoding", "utf8"),
          scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlint"),
          scalacOptions  += "-optimize",
          scalacOptions  += "-Yinline-warnings",
          scalacOptions  += "-Yno-adapted-args",
          scalacOptions  += "-Ywarn-value-discard",
    libraryDependencies  += "ch.qos.logback"          %  "logback-classic"        % "1.1.1",
    libraryDependencies  += "net.databinder.dispatch" %% "dispatch-core"          % "0.11.1",
    libraryDependencies  += "net.databinder.dispatch" %% "dispatch-json4s-native" % "0.11.1")
  settings (typelevelDefaultSettings: _*)
  settings (typelevelBuildInfoSettings: _*)
  settings (
    githubProject := ("beamly", "flumeback"),
       githubDevs := Seq(Developer("Dale Wijnand", "dwijnand"), Developer("Ali Asad Lotia", "lotia")),
       crossBuild := true,
    pgpPublicRing := Path.userHome / ".gnupg" / "beamly-pubring.gpg",
    pgpSecretRing := Path.userHome / ".gnupg" / "beamly-secring.gpg",
      profileName := "com.beamly")
  settings (site.settings: _*) settings (site.includeScaladoc(): _*)
  settings (ghpages.settings: _*)
  settings (git.remoteRepo := "git@github.com:beamly/flumeback.git")
)
