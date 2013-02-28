package models

import anorm._
import play.api.db._
import play.api.Play.current
import java.util.Date
import java.text.SimpleDateFormat
import models.renorm.Table

case class Comment(
                    userId: Long,
                    conferenceId: Long,
                    content: String,
                    createdAt: Date) {
    var id: Long = 0

    val dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")

    def isoDate = dateFormat.format(createdAt)

    implicit val instance = this
    lazy val user = User.byId(userId).get
}

object Comment extends Table[Comment] {
    def setRelatedToConference(originalConferenceId: Long, newConferenceId: Long) = DB.withConnection {
        implicit c =>
            SQL("UPDATE comment SET conference_id = {newConferenceId} WHERE conference_id = {originalConferenceId}").on(
                'newConferenceId -> newConferenceId,
                'originalConferenceId -> originalConferenceId).executeUpdate()
    }

    def create(conferenceId: Long, userId: Long, content: String, date: Date) =
        createEntry('user_id -> userId,
            'conference_id -> conferenceId,
            'content -> content,
            'created_at -> date)
}
