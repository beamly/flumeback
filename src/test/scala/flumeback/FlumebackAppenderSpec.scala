package flumeback

import ch.qos.logback.classic.spi.LoggingEvent
import ch.qos.logback.classic.{Level, Logger}
import com.ning.http.client.AsyncHandler.STATE
import com.ning.http.client._
import com.ning.http.client.providers.jdk.JDKFuture
import dispatch.Http
import org.slf4j.LoggerFactory
import org.specs2.mutable.Specification

import java.net.{HttpURLConnection, URL}
import java.util.concurrent.atomic.AtomicReference

class FlumebackAppenderSpec extends Specification {
  "The FlumebackAppender" should {
    "send properly formatted json" in {
      val ref = new AtomicReference[Request]()
      val flumebackAppender = new FlumebackAppender
      val http = Http(FakeAsyncHttpClient(ref))
      flumebackAppender.http = http
      flumebackAppender.start()

      val now = System.currentTimeMillis()
      val loggerName = s"TestClass@$now"
      val logger = LoggerFactory.getLogger(loggerName).asInstanceOf[Logger]

      val le = new LoggingEvent("TestClass", logger, Level.INFO, "Hi", /*throwable = */null, /*argArray = */null)
      le.setTimeStamp(now)

      flumebackAppender.doAppend(le)

      val request = ref.get()
      request.getStringData ====
        s"""[{
          |   "headers" : {"timestamp":"$now","level":"INFO","threadId":"specs2.DefaultExecutionStrategy-1","source":"$loggerName"},
          |   "body" : "Hi"
          |}]
          |""".stripMargin
    }
  }
}

case class FakeAsyncHttpClient(ref: AtomicReference[Request]) extends AsyncHttpClient {
  override def executeRequest[T](request: Request, handler: AsyncHandler[T]): ListenableFuture[T] = {
    ref.set(request)
    new JDKFuture[T](FakeAsyncHandler(), 0, FakeHttpURLConnection())
  }
}

case class FakeAsyncHandler[T]() extends AsyncHandler[T] {
  def onThrowable(t: Throwable) = ()
  def onCompleted()             = null.asInstanceOf[T]

  def onBodyPartReceived(bodyPart: HttpResponseBodyPart)   = STATE.CONTINUE
  def onStatusReceived(responseStatus: HttpResponseStatus) = STATE.CONTINUE
  def onHeadersReceived(headers: HttpResponseHeaders)      = STATE.CONTINUE
}

case class FakeHttpURLConnection() extends HttpURLConnection(new URL("http://google.com")) {
  def connect(): Unit       = ()
  def disconnect(): Unit    = ()
  def usingProxy(): Boolean = false
}
