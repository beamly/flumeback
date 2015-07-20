import ch.qos.logback.classic.{ LoggerContext, Logger }
import org.slf4j.LoggerFactory
import org.slf4j.helpers.SubstituteLoggerFactory

package object flumeback {
  @volatile private var loggingInitialised = false

  def getLogger(loggerName: String): Logger = {
    while (!loggingInitialised) {
      LoggerFactory.getILoggerFactory match {
        case logger: SubstituteLoggerFactory => Thread sleep 100
        case logger: LoggerContext           => loggingInitialised = true
      }
    }
    LoggerFactory.getLogger(loggerName).asInstanceOf[Logger]
  }
}
