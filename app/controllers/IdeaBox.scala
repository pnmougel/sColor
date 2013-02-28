package controllers

import play.api.data.Forms._
import play.api.mvc._
import controllers.AuthenticationActions._
import play.api.data.Form
import java.util.Date
import models.Idea

object IdeaBox extends Controller {
    val addIdeaForm = Form(
        tuple(
            "name" -> text,
            "description" -> text))

    def index = Authentication {
        implicit request =>
            Ok(views.html.ideabox(Idea.byNbVotes(), request.user))
    }

    def addIdea() = Authentication {
        implicit request =>
            addIdeaForm.bindFromRequest.fold(
                errors => BadRequest("How did you manage that ?"),
                params => params match {
                    case (name, description) =>
                        if (request.user.isDefined) {
                            models.Idea.create(name, description, new Date(), request.user.get.id)
                            Ok(views.html.ideabox(Idea.byDate(), request.user))
                        } else {
                            Ok(views.html.ideabox(Idea.byDate(), request.user))
                        }
                })
    }

    def vote(ideaId: Long) = Authentication {
        implicit request =>
            if (request.user.isDefined) {
                models.IdeaVote.create(ideaId, request.user.get.id)
                Ok
            } else {
                BadRequest
            }
    }
}