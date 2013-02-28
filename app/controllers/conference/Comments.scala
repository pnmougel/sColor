package controllers.conference

import java.util.Date

import models.Comment
import play.api.data.Forms.number
import play.api.data.Forms.text
import play.api.data.Forms.tuple
import play.api.mvc.Controller
import play.api.data.Form
import controllers.AuthenticationActions._

object Comments extends Controller {

    val commentForm = Form(
        tuple(
            "conference_id" -> number,
            "content" -> text))

    def addComment = Authentication {
        implicit request =>
            commentForm.bindFromRequest.fold(
                errors => BadRequest("How did you manage that ?"),
                comment => comment match {
                    case (conferenceId: Int, content: String) => {
                        if (request.user.isDefined) {
                            val commentId = Comment.create(conferenceId, request.user.get.id, content, new Date())
                            val insertedComment = Comment.byId(commentId).get
                            Ok(views.html.snippets.comment(insertedComment, request.user))
                        } else {
                            Ok
                        }
                    }
                }
            )
    }


    def deleteComment(id: Long) = Authentication {
        implicit request =>
            if (request.user.isDefined) {
                val curUser = request.user.get
                if (curUser.isAdmin || {
                    val curComment = Comment.byId(id)
                    curComment.isDefined && curComment.get.userId == curUser.id
                }) {
                    Comment.delete(id)
                }
            }
            Ok
    }
}

