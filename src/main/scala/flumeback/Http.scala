package flumeback

import scala.concurrent.{ ExecutionContext, Future }
import java.net._

trait Http {
  def post(host: String, port: Int, json: String)(implicit ec: ExecutionContext): Future[Int]
}

object Http extends Http {
  def post(host: String, port: Int, json: String)(implicit ec: ExecutionContext): Future[Int] = {
    Future {
      val uri = URI.create(s"http://$host:$port/")
      val url = uri.toURL

      var conn: HttpURLConnection = null
      try {
        conn = url.openConnection().asInstanceOf[HttpURLConnection]
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        conn.setRequestMethod("POST")
        conn.setDoOutput(true)

        val os = conn.getOutputStream()
        os.write(json.getBytes("UTF-8"))
        os.flush()
        os.close()

        conn.getResponseCode()
      } finally {
        if (conn ne null) conn.disconnect()
      }
    }
  }
}
