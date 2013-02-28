package controllers

import play.api.mvc._
import models.Conference

object Widget extends Controller {
    def getBadge(conferenceId: Long, size: Int = 12) = Action {
        implicit request =>
            val conference = Conference.byId(conferenceId)
            if (conference.isDefined) {
                Ok(views.html.snippets.widget(conference.get.avgScore))
            } else {
                Ok(views.html.snippets.widget(None))
            }
    }

    def getHindex(conferenceId: Long) = Action {
        implicit request =>
            val conference = Conference.byId(conferenceId)
            if (conference.isDefined) {
                Ok("")
            } else {
                Ok("")
            }
    }
}