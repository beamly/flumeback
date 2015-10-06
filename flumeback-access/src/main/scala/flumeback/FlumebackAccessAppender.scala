package flumeback

import ch.qos.logback.access.pattern.DateConverter
import ch.qos.logback.access.spi.IAccessEvent
import ch.qos.logback.core.{ CoreConstants, AppenderBase }

import scala.collection.JavaConverters._
import java.net.{ UnknownHostException, InetAddress }

// Add all request parameters?
// Add all request headers?
// Add all request attributes?
// Add all cookies?
// Add all response headers?
// Add all request content?
// Add all response content?
class FlumebackAccessAppender extends AppenderBase[IAccessEvent] {
  def append(ae: IAccessEvent): Unit = {
    val localIp =
      try InetAddress.getLocalHost.getHostAddress catch { case _: UnknownHostException => "127.0.0.1" }

    val contentLength = if (ae.getContentLength == -1) "-" else ae.getContentLength.toString

    val dateConverter = new DateConverter
    dateConverter.setOptionList(Seq("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "UTC").asJava)
    dateConverter.start()

    val date = dateConverter.convert(ae)
    val remoteUser = Option(ae.getRemoteUser).getOrElse("-")

    val headers = Map[String, String](
           "remoteIP" -> ae.getRemoteAddr,
         "remoteAddr" -> ae.getRemoteAddr,
            "localIP" -> localIp,
          "localAddr" -> localIp,
          "bytesSent" -> contentLength,
      "contentLength" -> contentLength,
         "clientHost" -> ae.getRemoteHost,
         "remoteHost" -> ae.getRemoteHost,
           "protocol" -> ae.getProtocol,
      "requestMethod" -> ae.getMethod,
             "method" -> ae.getMethod,
         "requestURL" -> ae.getRequestURL,
         "statusCode" -> ae.getStatusCode.toString,
          "timestamp" -> ae.getTimeStamp.toString,
               "date" -> date,
               "user" -> ae.getRemoteUser,
         "remoteUser" -> remoteUser,
         "requestURI" -> ae.getRequestURI,
             "server" -> ae.getServerName,
         "serverName" -> ae.getServerName,
          "localPort" -> ae.getLocalPort.toString,
        "elapsedTime" -> ae.getElapsedTime.toString
    )

    val referer     = ae.getRequestHeader("Referer")
    val userAgent   = ae.getRequestHeader("User-Agent")
    val elapsedTime = ae.getElapsedTime.toString
    val requestId   = ae.getRequestHeader("X-Request-Id")

    val msg = {
      val h = ae.getRemoteHost
      val user = remoteUser
      val t = date
      val r = ae.getRequestURL
      val s = ae.getStatusCode.toString
      val b = contentLength
      s"""$h $user [$t] "$r" $s $b "$referer" "$userAgent" $elapsedTime [$requestId]"""
    }

    def fullRequest = {
      val buf: StringBuilder = new StringBuilder

      val headerNames: java.util.Enumeration[_] = ae.getRequestHeaderNames
      while (headerNames.hasMoreElements) {
        val name: String = headerNames.nextElement.asInstanceOf[String]
        buf.append(name)
        buf.append(": ")
        buf.append(ae.getRequestHeader(name))
        buf.append(CoreConstants.LINE_SEPARATOR)
      }
      buf.append(CoreConstants.LINE_SEPARATOR)
      buf.append(ae.getRequestContent)
      buf.toString
    }

    def fullResponse = {
      val buf: StringBuilder = new StringBuilder

      buf.append("HTTP/1.1 ")
      val statusCode: Int = ae.getStatusCode
      buf.append(statusCode)
      buf.append(" ")
      buf.append(getStatusCodeDescription(statusCode))
      buf.append(CoreConstants.LINE_SEPARATOR)

      import scala.collection.JavaConverters._
      val hnList = ae.getResponseHeaderNameList.asScala
      for (headerName <- hnList) {
        buf.append(headerName)
        buf.append(": ")
        buf.append(ae.getResponseHeader(headerName))
        buf.append(CoreConstants.LINE_SEPARATOR)
      }
      buf.append(CoreConstants.LINE_SEPARATOR)
      buf.append(ae.getResponseContent)
      buf.append(CoreConstants.LINE_SEPARATOR)
      buf.toString
    }

    def getStatusCodeDescription(sc: Int): String = {
      sc match {
        case 200 => "OK"
        case 201 => "Created"
        case 202 => "Accepted"
        case 203 => "Non-Authoritative Information"
        case 204 => "No Content"
        case 205 => "Reset Content"
        case 206 => "Partial Content"
        case 300 => "Multiple Choices"
        case 301 => "Moved Permanently"
        case 302 => "Found"
        case 303 => "See Other"
        case 304 => "Not Modified"
        case 305 => "Use Proxy"
        case 306 => "(Unused)"
        case 307 => "Temporary Redirect"
        case 400 => "Bad Request"
        case 401 => "Unauthorized"
        case 402 => "Payment Required"
        case 403 => "Forbidden"
        case 404 => "Not Found"
        case 405 => "Method Not Allowed"
        case 406 => "Not Acceptable"
        case 407 => "Proxy Authentication Required"
        case 408 => "Request Timeout"
        case 409 => "Conflict"
        case 410 => "Gone"
        case 411 => "Length Required"
        case 412 => "Precondition Failed"
        case 413 => "Request Entity Too Large"
        case 414 => "Request-URI Too Long"
        case 415 => "Unsupported Media Type"
        case 416 => "Requested Range Not Satisfiable"
        case 417 => "Expectation Failed"
        case 500 => "Internal Server Error"
        case 501 => "Not Implemented"
        case 502 => "Bad Gateway"
        case 503 => "Service Unavailable"
        case 504 => "Gateway Timeout"
        case 505 => "HTTP Version Not Supported"
        case _   => "NA"
      }
    }
  }
}
