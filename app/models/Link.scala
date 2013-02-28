package models

import anorm._
import play.api.db._
import play.api.Play.current
import java.util.Date
import models.renorm.Table

case class Link(
                 conferenceId: Long,
                 url: String,
                 name: Option[String],
                 userId: Long) {
    var id: Long = 0
}

object Link extends Table[Link] {
    def setRelatedToConference(originalConferenceId: Long, newConferenceId: Long) = DB.withConnection {
        implicit c =>
            SQL("UPDATE link SET conference_id = {newConferenceId} WHERE conference_id = {originalConferenceId}").on(
                'newConferenceId -> newConferenceId,
                'originalConferenceId -> originalConferenceId).executeUpdate()
    }

    def getByConferenceId(conferenceId: Long) = find("conference_id" -> conferenceId)

    def create(conferenceId: Long, url: String, label: String, date: Date, userId: Long): Long =
        createEntry('conference_id -> conferenceId, 'url -> url, 'name -> label, 'created_at -> date, 'user_id -> userId)
}