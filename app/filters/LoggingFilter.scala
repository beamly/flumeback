package filters

import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{DateTime, DateTimeZone}
import org.slf4j.LoggerFactory
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._

object LoggingFilter extends EssentialFilter {
  val log = LoggerFactory.getLogger("HttpAccessLogger")
  val dateTimeFmt = ISODateTimeFormat.dateTime()

  def apply(nextFilter: EssentialAction) = new EssentialAction {
    def apply(requestHeader: RequestHeader) = {
      val startTime = DateTime now DateTimeZone.UTC
      nextFilter(requestHeader) map { result =>
        val endTime = DateTime now DateTimeZone.UTC
        val requestTime = endTime.getMillis - startTime.getMillis

        val startTimeStr = startTime.toString(dateTimeFmt)
        val httpMethodStr = requestHeader.method.padTo(4, " ").mkString
        val resultStatusCode = result.header.status
        val requestTimeStr = (requestTime + "ms").padTo(6, " ").mkString
        val requestUri = requestHeader.uri

        log info s"$startTimeStr  $httpMethodStr $resultStatusCode  $requestTimeStr $requestUri"

        result withHeaders "Request-Time" -> requestTime.toString
      }
    }
  }
}
