package flumeback

import ch.qos.logback.classic.pattern.ThrowableProxyConverter
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.{AppenderBase, CoreConstants}
import dispatch.Defaults._
import dispatch._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration._

class FlumebackAppender extends AppenderBase[ILoggingEvent] {
  val throwableProxyConverter = new ThrowableProxyConverter

  override def start(): Unit = {
    throwableProxyConverter.start()

    super.start()
  }

  def append(le: ILoggingEvent): Unit = {
    val message = le.getFormattedMessage
    val stackStr = throwableProxyConverter.convert(le)
    val body = message + CoreConstants.LINE_SEPARATOR + stackStr

    val headers = Map(
      "timestamp" -> le.getTimeStamp.toString,
          "level" -> le.getLevel.toString,
       "threadId" -> le.getThreadName,
         "source" -> le.getLoggerName
    ) ++ le.getMDCPropertyMap.asScala.toMap
    val headersStr = compact(render(headers))

    val resp = Http((
      host("localhost", 16002)
      setContentType("application/json", "UTF-8")
    ) << s"""
        |[{
        |   "headers" : $headersStr,
        |   "body" : "$body"
        |}]
      """.stripMargin
    )
    Await.result(resp, 1.minute)
  }
}
