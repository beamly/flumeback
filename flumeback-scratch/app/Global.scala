import org.joda.time.DateTime
import play.Logger
import play.api.Application
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.mvc.WithFilters

import scala.concurrent.duration._

object Global extends WithFilters(playpen.HttpAccessLoggingFilter) {
  lazy implicit val actorSystem = Akka.system
  lazy implicit val execContext = actorSystem.dispatcher

  override def onStart(app: Application): Unit = {
    Logger info s"Application has started at ${new DateTime()}"
    actorSystem.scheduler.schedule(1.minute, 1.minute)(Logger info s"Tick: ${new DateTime()}")
  }
}
