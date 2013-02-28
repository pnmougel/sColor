package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import models.renorm.Table

case class SubField(name: String, fieldId: Long) {
    var id: Long = 0

    implicit val instance = this
    lazy val field = Field.byId(fieldId).get


    def countConferencesWithSubfield: Long = DB.withConnection {
        implicit c =>
            SQL("SELECT count(*) FROM conference_subfield WHERE subfield_id = {subfieldId}").on(
                'subfieldId -> id).as(scalar[Long].single)
    }
}

object SubField extends Table[SubField] {
    def getByName(name: String) = findOption('name -> name)

    def getByField(id: Long): List[SubField] = DB.withConnection {
        implicit c =>
            SQL("SELECT * FROM subfield WHERE field_id = {id} ORDER BY name").on('id -> id).as(single *)
    }

    def getByFieldAndName(name: String, fieldId: Long) = findOption('field_id -> fieldId, 'name -> name)


    def setRelatedToConference(originalConferenceId: Long, newConferenceId: Long) = DB.withConnection {
        implicit c =>
            SQL("UPDATE conference_subfield SET conference_id = {newConferenceId} WHERE conference_id = {originalConferenceId}").on(
                'newConferenceId -> newConferenceId,
                'originalConferenceId -> originalConferenceId).executeUpdate()
    }

    def getOrCreate(name: String, fieldId: Long): Long = {
        val subField = getByFieldAndName(name, fieldId)
        if (subField.isDefined) {
            subField.get.id
        } else {
            create(name, fieldId)
        }
    }


    def create(name: String, fieldId: Long): Long = createEntry('name -> name, 'field_id -> fieldId)
}