package flumeback

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import dispatch.Defaults._
import dispatch._

class FlumebackAppender extends AppenderBase[ILoggingEvent] {
  def append(le: ILoggingEvent): Unit = {
    Http {
      host("localhost", 16002).setContentType("application/json", "UTF-8") <<
        s"""
        |[{
        |   "headers" : {
        |              "timestamp" : "${le.getTimeStamp}",
        |              "host" : "random_host.example.com"
        |   },
        |   "body" : "${le.getFormattedMessage}"
        |}]
      """.stripMargin
    }
  }
}
