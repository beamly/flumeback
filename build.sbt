import SbtMisc._

lazy val commonSetup: Project => Project = (_
  enablePlugins BuildInfoPlugin
  settings (
    organization := "com.beamly.flumeback",
        licenses := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
     description := "A Logback appender for Flume",
        homepage := Some(url(s"https://github.com/beamly/flumeback")),
       startYear := Some(2014),

    scalaVersion := "2.11.7",
    crossScalaVersions := Seq(scalaVersion.value, "2.10.6"),

    scalacOptions ++= Seq("-encoding", "utf8"),
    scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlint"),
    scalacOptions  += "-language:higherKinds",
    scalacOptions  += "-language:implicitConversions",
    scalacOptions  += "-language:postfixOps",
    scalacOptions  += "-Xfatal-warnings",
    scalacOptions  += "-Xfuture",
    scalacOptions  += "-Yno-adapted-args",
    scalacOptions  += "-Ywarn-dead-code",
    scalacOptions  += "-Ywarn-numeric-widen",
    scalacOptions ++= "-Ywarn-unused-import".ifScala211Plus.value.toSeq,
    scalacOptions  += "-Ywarn-value-discard",

    scalacOptions in (Compile, console) -= "-Ywarn-unused-import",
    scalacOptions in (Test,    console) -= "-Ywarn-unused-import",

    maxErrors := 5,
    triggeredMessage := Watched.clearWhenTriggered,

    libraryDependencies += "ch.qos.logback"  % "logback-classic" % logbackVersion.value,
    libraryDependencies += "org.json4s"     %% "json4s-core"     % "3.2.9",
    libraryDependencies += "org.json4s"     %% "json4s-native"   % "3.2.9",
    libraryDependencies += "org.specs2"     %% "specs2"          % "2.4.2" % "test",

    buildInfoOptions += BuildInfoOption.BuildTime,
    buildInfoPackage := name.value.map(ch => if (ch == '-') '.' else ch),
    buildInfoUsePackageAsPath := true,

    parallelExecution in Test := true,
    fork in Test := false,

    bintrayOrganization := Some("beamly"),

    pomExtra := pomExtra.value ++ {
        <developers>
            <developer>
                <id>dwijnand</id>
                <name>Dale Wijnand</name>
                <email>dale wijnand gmail com</email>
                <url>dwijnand.com</url>
            </developer>
            <developer>
                <id>lotia</id>
                <name>Ali Asad Lotia</name>
            </developer>
        </developers>
        <scm>
            <connection>scm:git:github.com/beamly/flumeback.git</connection>
            <developerConnection>scm:git:git@github.com:beamly/flumeback.git</developerConnection>
            <url>https://github.com/beamly/flumeback</url>
        </scm>
    },

    releaseCrossBuild := true
  )
)

lazy val flumebackRoot = (project in file(".") configure commonSetup settings noArtifacts
  dependsOn (`flumeback-core`, flumeback, `flumeback-access`)
  aggregate (`flumeback-core`, flumeback, `flumeback-access`)
)

lazy val `flumeback-core` = project configure commonSetup

lazy val flumeback = project dependsOn `flumeback-core` configure commonSetup

lazy val `flumeback-access` = project dependsOn `flumeback-core` configure commonSetup settings(
  libraryDependencies += "ch.qos.logback" % "logback-access"    % logbackVersion.value,
  libraryDependencies += "javax.servlet"  % "javax.servlet-api" % "3.1.0" % "provided"
)

val logbackVersion = settingKey[String]("")
logbackVersion in ThisBuild := "1.1.3"

def Settings(settings: SettingsDefinition*): Seq[Setting[_]] = settings.flatMap(_.settings)

val noPackage = Settings(Keys.`package` := file(""), packageBin := file(""), packagedArtifacts := Map())
val noPublish = Settings(publish := {}, publishLocal := {}, publishArtifact := false)
val noArtifacts = Settings(noPackage, noPublish)

watchSources ++= (baseDirectory.value * "*.sbt").get
watchSources ++= (baseDirectory.value / "project" * "*.scala").get
