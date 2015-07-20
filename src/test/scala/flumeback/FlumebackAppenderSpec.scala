package flumeback

import ch.qos.logback.classic.spi.LoggingEvent
import ch.qos.logback.classic.{ Level, Logger }
import org.json4s._
import org.json4s.native.JsonMethods._
import org.slf4j.LoggerFactory
import org.specs2.execute.{ AsResult, Result }
import org.specs2.mutable.Specification
import org.specs2.specification.FixtureExample

import scala.concurrent.{ ExecutionContext, Future }
import java.net.HttpURLConnection.HTTP_OK
import java.util.concurrent.atomic.AtomicReference

class FlumebackAppenderSpec extends Specification with ContextFixture {
  "The FlumebackAppender" should {

    "send properly formatted json" in { c: Context =>
      import c._

      val le = new LoggingEvent("TestClass", logger, Level.INFO, "Hi", /*throwable = */null, /*argArray = */null)
      le setTimeStamp now

      flumebackAppender doAppend le

      req.get() ====
        s"""[{
          |  "headers" : {"timestamp":"$now","level":"INFO","threadId":"$currentThreadName","source":"$loggerName"},
          |  "body" : "Hi"
          |}]
          |""".stripMargin
    }

    "escape log messages when constructing the json payload" in { c: Context =>
      import c._

      val msg = """{"a":"b\c"}"""
      val le = new LoggingEvent("TestClass", logger, Level.INFO, msg, /*throwable = */null, /*argArray = */null)
      le setTimeStamp now

      flumebackAppender doAppend le

      req.get() ====
        s"""[{
          |  "headers" : {"timestamp":"$now","level":"INFO","threadId":"$currentThreadName","source":"$loggerName"},
          |  "body" : "{\\"a\\":\\"b\\\\c\\"}"
          |}]
          |""".stripMargin

      parse(req.get()) must beLike {
        case JArray(List(JObject(List(_, ("body", JString(body)))))) => body ==== msg
      }
    }
  }
}

case class Context(flumebackAppender: FlumebackAppender, req: AtomicReference[String]) {
  val now = System.currentTimeMillis()
  val loggerName = s"TestClass@$now"
  val logger = getLogger(loggerName)
  val currentThreadName = Thread.currentThread.getName
}

trait ContextFixture extends FixtureExample[Context] {
  protected def fixture[R: AsResult](f: (Context) => R): Result = {
    val req = new AtomicReference[String]()
    val flumebackAppender = new FlumebackAppender
    flumebackAppender.http = FakeHttp(req)
    flumebackAppender.start()

    try     AsResult(f(Context(flumebackAppender, req)))
    finally flumebackAppender.stop()
  }
}

case class FakeHttp(ref: AtomicReference[String]) extends Http {
  def post(host: String, port: Int, json: String)(implicit ec: ExecutionContext): Future[Int] = {
    ref set json
    Future successful HTTP_OK
  }
}
