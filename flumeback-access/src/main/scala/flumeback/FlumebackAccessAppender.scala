package flumeback

import ch.qos.logback.access.pattern.DateConverter
import ch.qos.logback.access.spi.IAccessEvent
import ch.qos.logback.core.AppenderBase
import org.json4s.ParserUtil

import scala.beans.BeanProperty
import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

class FlumebackAccessAppender extends AppenderBase[IAccessEvent] {
  @BeanProperty var host = "localhost"
  @BeanProperty var port = 16301
  implicit var executor = ExecutionContext.global

  private[flumeback] var http: Http = Http

  private[flumeback] var dateConverter = {
    val dateConverter = new DateConverter
    dateConverter.setOptionList(Seq("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "UTC").asJava)
    dateConverter
  }

  private val allowedRepeats = 5
  private var exceptionCount = 0

  override def start(): Unit = {
    dateConverter.start()
    super.start()
  }

  override def stop(): Unit = {
    super.stop()
    dateConverter.stop()
  }

  def append(ae: IAccessEvent): Unit = {
    val contentLength = if (ae.getContentLength == -1) "-" else ae.getContentLength.toString

    val msg = {
      val h = ae.getRemoteHost
      val user = Option(ae.getRemoteUser) getOrElse "-"
      val t = dateConverter convert ae
      val r = ae.getRequestURL
      val s = ae.getStatusCode
      val b = contentLength
      val referer = ae getRequestHeader "Referer"
      val userAgent = ae getRequestHeader "User-Agent"
      val elapsedTime = ae.getElapsedTime
      val requestId = ae getRequestHeader "X-Request-Id"
      s"""$h $user [$t] "$r" $s $b "$referer" "$userAgent" $elapsedTime [$requestId]"""
    }

    val body = s"""[{"body" : "${ParserUtil quote msg}"}]"""

    val resp = http.post(host, port, body)

    resp onFailure {
      case NonFatal(e) =>
        if (exceptionCount < allowedRepeats)
          addError("Appender [" + name + "] failed to append.", e)
        exceptionCount += 1
    }
  }
}
