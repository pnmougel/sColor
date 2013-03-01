package models

import java.util.Date
import anorm._
import anorm.SqlParser._
import play.api.Play.current
import play.api.db._
import models.renorm.OrderBy
import models.renorm.Table
import java.text.SimpleDateFormat

case class Idea(var name: String, description: String, done: Boolean, createdAt: Date, userId: Long) {
    var id: Long = 0

    lazy val user = User.byId(userId)

    val dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")

    def isoDate = dateFormat.format(createdAt)

    lazy val nbVotes = DB.withConnection {
        implicit c =>
            SQL("SELECT COUNT(*) FROM ideavote WHERE idea_id = {ideaId}").on(
                'ideaId -> id).as(scalar[Long].single)
    }
}

object Idea extends Table[Idea] {
    def byDate() = super.all(OrderBy("created_at", "DESC"))

    def byNbVotes() = DB.withConnection {
        implicit c =>
            SQL("SELECT idea.*, COUNT(*) as nbVotes FROM idea, ideavote WHERE ideavote.idea_id = idea.id GROUP BY idea.id ORDER BY nbVotes DESC").as(single *)
    }

    def create(name: String, description: String, createdAt: Date, userId: Long): Long = {
        val ideaId = super.getOrCreate('name -> name, 'description -> description, 'created_at -> createdAt, 'user_id -> userId, 'done -> false)
        IdeaVote.createEntry('idea_id -> ideaId, 'user_id -> userId)
        ideaId
    }
}