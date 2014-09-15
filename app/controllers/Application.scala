package controllers

import org.joda.time.DateTime
import play.api._
import play.api.mvc._

object Application extends Controller {
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def datetime = Action {
    Ok(new DateTime().toString())
  }
}
