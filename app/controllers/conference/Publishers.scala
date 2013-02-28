package controllers.conference

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models.Publisher

object Publishers extends Controller {
    val form = Form("name" -> text)

    def add = Action {
        implicit request =>
            form.bindFromRequest.fold(
                errors => BadRequest(""),
                params => params match {
                    case (name) =>
                        Publisher.create(name)
                        Ok
                }
            )
    }

    def delete(id: Long) = Action {
        implicit request =>
            Publisher.delete(id)
            Ok
    }
}

