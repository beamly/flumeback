package flumeback

import ch.qos.logback.classic.pattern.{ ThrowableHandlingConverter, ThrowableProxyConverter }
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.{ AppenderBase, CoreConstants }
import org.json4s.JsonDSL._
import org.json4s.ParserUtil
import org.json4s.native.JsonMethods._

import scala.beans.BeanProperty
import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

class FlumebackAppender extends AppenderBase[ILoggingEvent] {
  @BeanProperty var host = "localhost"
  @BeanProperty var port = 16301
  var throwableHandlingConverter: ThrowableHandlingConverter = new ThrowableProxyConverter
  implicit var executor = ExecutionContext.global

  private[flumeback] var http: Http = Http

  private val allowedRepeats = 5
  private var exceptionCount = 0

  override def start(): Unit = {
    throwableHandlingConverter.start()
    super.start()
  }

  override def stop(): Unit = {
    super.stop()
    throwableHandlingConverter.stop()
  }

  def append(le: ILoggingEvent): Unit = {
    val message = le.getFormattedMessage
    val stackStr = throwableHandlingConverter convert le
    val fullMessage =
      if (stackStr == "") message
      else                message + CoreConstants.LINE_SEPARATOR + stackStr

    val headers = Map(
      "timestamp" -> le.getTimeStamp.toString,
          "level" -> le.getLevel.toString,
       "threadId" -> le.getThreadName,
         "source" -> le.getLoggerName
    ) ++ le.getMDCPropertyMap.asScala.toMap
    val headersStr = compact(render(headers))

    val body = s"""[{
        |  "headers" : $headersStr,
        |  "body" : "${ParserUtil quote fullMessage}"
        |}]
        |""".stripMargin

    val resp = http.post(host, port, body)

    resp onFailure {
      case NonFatal(e) =>
        if (exceptionCount < allowedRepeats)
          addError("Appender [" + name + "] failed to append.", e)
        exceptionCount += 1
    }
  }
}
