import sbtrelease._, ReleasePlugin.ReleaseKeys._

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
  settings (releaseSettings: _*) settings (sonatypeSettings: _*) settings (
/**
 * Release versioning:
 * All minor releases have a bugfix version of 0.
 * To create a new bugfix release, checkout the v{x}.{y}.0 tagged release as branch v{x}.{y}.
 * All bugfix releases for that minor version should be created from that branch.
 * The bugfix version should automatically increment within that branch.
 */
    releaseVersion := { ver =>
      Version(ver) map { v =>
        v.copy(bugfix = v.bugfix map (_ max 1) orElse Some(0)).withoutQualifier.string
      } getOrElse versionFormatError
    },
    nextVersion    := { ver =>
      Version(ver) map { v =>
        v.bugfix collect {
          case n if n > 0 => v.bumpBugfix.string
        } getOrElse          v.bumpMinor.copy(bugfix = None).asSnapshot.string
      } getOrElse versionFormatError
    },
             pgpPublicRing := Path.userHome / ".gnupg" / "beamly-pubring.gpg",
             pgpSecretRing := Path.userHome / ".gnupg" / "beamly-secring.gpg",
    publishArtifactsAction := PgpKeys.publishSigned.value,
                  pomExtra :=
      <scm>
        <url>git@github.com:beamly/flumeback.git</url>
        <connection>scm:git:git@github.com:beamly/flumeback.git</connection>
      </scm>
      <developers>
        <developer>
          <id>dwijnand</id>
          <name>Dale Wijnand</name>
          <url>https://github.com/dwijnand</url>
        </developer>
        <developer>
          <id>lotia</id>
          <name>Ali Asad Lotia</name>
          <url>https://github.com/lotia</url>
        </developer>
      </developers>)
)
