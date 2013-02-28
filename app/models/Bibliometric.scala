package models

import anorm._
import models.renorm.Table

case class Bibliometric(name: String, value: Double, sourceId: Long) {
    var id: Long = 0

    lazy val source = BibliometricSource.byId(sourceId)
}

object Bibliometric extends Table[Bibliometric] {
    def createOrUpdate(conferenceId: Long, name: String, value: Double, sourceId: Long): Long = {
        val bibliometric = findOption('conference_id -> conferenceId, 'name -> name, 'source_id -> sourceId)
        if (bibliometric.isDefined) {
            update(bibliometric.get.id, 'value -> value)
        } else {
            createEntry('conference_id -> conferenceId, 'name -> name, 'value -> value, 'source_id -> sourceId)
        }
    }
}