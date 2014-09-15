lazy val root = (
  project in file(".")
  enablePlugins PlayScala
  settings (
             name  := "flumeback-scratch",
          version  := "0.1-SNAPSHOT",
     scalaVersion  := "2.11.2",
    scalacOptions  += "-deprecation",
    scalacOptions ++= Seq("-encoding", "utf8"),
    scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "Xlint"),
    scalacOptions  += "-optimize",
    scalacOptions  += "-Yinline-warnings",
    scalacOptions  += "-Yno-adapted-args",
    scalacOptions  += "-Ywarn-value-discard")
)
