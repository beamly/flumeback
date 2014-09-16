lazy val root = (
  project in file(".")
  enablePlugins PlayScala
  settings (
                   name  := "flumeback-scratch",
                version  := "0.1-SNAPSHOT",
           scalaVersion  := "2.11.2",
          scalacOptions ++= Seq("-encoding", "utf8"),
          scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlint"),
          scalacOptions  += "-optimize",
          scalacOptions  += "-Yinline-warnings",
          scalacOptions  += "-Yno-adapted-args",
          scalacOptions  += "-Ywarn-value-discard",
    libraryDependencies  += "com.beamly.flumeback" %% "flumeback" % "0.1-SNAPSHOT")
)
