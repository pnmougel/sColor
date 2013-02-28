package controllers.conference

import java.util.Date

import models.UserVote
import play.api.data.Forms.number
import play.api.data.Forms.tuple
import play.api.data.Form
import play.api.mvc.Controller
import controllers.AuthenticationActions._

object Scores extends Controller {
    val userScoreForm = Form(
        tuple(
            "conference_id" -> number,
            "score" -> number))
    val deleteVoteForm = Form("conference_id" -> number)

    def addVote = Authentication {
        implicit request =>
            userScoreForm.bindFromRequest.fold(
                errors => BadRequest("How did you manage that ?"),
                params => params match {
                    case (conferenceId, score) =>
                        if (request.user.isDefined) {
                            // TODO: Ensure that there is no double votes here
                            UserVote.create(conferenceId, request.user.get.id, score, new Date())
                            val userVotes = UserVote.getUserVotesByConferenceId(conferenceId)
                            Ok(views.html.conferences.userScores(userVotes))
                        } else {
                            Ok
                        }
                }
            )
    }

    def deleteVote = Authentication {
        implicit request =>
            deleteVoteForm.bindFromRequest.fold(
                errors => BadRequest("How did you manage that ?"),
                params => params match {
                    case (conferenceId) =>
                        if (request.user.isDefined) {
                            UserVote.delete(conferenceId, request.user.get.id)
                            val userVotes = UserVote.getUserVotesByConferenceId(conferenceId)
                            Ok(views.html.conferences.userScores(userVotes))
                        } else {
                            Ok
                        }
                }
            )
    }
}

