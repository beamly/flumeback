import TypelevelKeys._
import org.typelevel.sbt.ReleaseSeries
import org.typelevel.sbt.Version._

series in ThisBuild := ReleaseSeries(0,1)

relativeVersion in ThisBuild := Relative(2,Snapshot)

lastRelease in ThisBuild := Relative(0,Final)
