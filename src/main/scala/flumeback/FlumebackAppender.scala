package flumeback

import ch.qos.logback.classic.pattern.ThrowableProxyConverter
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.{AppenderBase, CoreConstants}
import dispatch.Defaults._
import dispatch._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._

import scala.beans.BeanProperty
import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.control.NonFatal

class FlumebackAppender extends AppenderBase[ILoggingEvent] {
  val throwableProxyConverter = new ThrowableProxyConverter

  @BeanProperty var host = "localhost"
  @BeanProperty var port = 16311
  @BeanProperty var timeout = "1 second"

  private var await: Duration = 1.second
  private[flumeback] var http: Http = Http

  override def start(): Unit = {
    throwableProxyConverter.start()

    try {
      await = Duration(timeout)
      super.start()
    } catch {
      case NonFatal(e) =>
        addError(s"Failed to parse timeout: $timeout", e)
    }
  }

  override def stop(): Unit = {
    super.stop()
    http.client.close()
  }

  def append(le: ILoggingEvent): Unit = {
    val message = le.getFormattedMessage
    val stackStr = throwableProxyConverter.convert(le)
    val body =
      if (stackStr == "") message
      else                message + CoreConstants.LINE_SEPARATOR + stackStr

    val headers = Map(
      "timestamp" -> le.getTimeStamp.toString,
          "level" -> le.getLevel.toString,
       "threadId" -> le.getThreadName,
         "source" -> le.getLoggerName
    ) ++ le.getMDCPropertyMap.asScala.toMap
    val headersStr = compact(render(headers))

    val resp = http((
      dispatch.host(host, port)
      setContentType("application/json", "UTF-8")
    ) << s"""[{
        |   "headers" : $headersStr,
        |   "body" : "$body"
        |}]
        |""".stripMargin
    )

    Await.result(resp, await)
    ()
  }
}
