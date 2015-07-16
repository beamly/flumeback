import sbt._
import sbt.Keys._

object SbtKitPre {
  def scalaPartV = Def.setting(CrossVersion partialVersion scalaVersion.value)

  implicit final class AnyWithIfScala11Plus[A](val _o: A) {
    def ifScala211Plus = Def.setting(scalaPartV.value collect { case (2, y) if y >= 11 => _o })
  }
}
