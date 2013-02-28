package models

import anorm._
import anorm.SqlParser._
import models.renorm.Table

case class ConferenceConference(conferenceFromId: Long, conferenceToId: Long, relationtypeId: Long)

object ConferenceConference extends Table[ConferenceConference](Option("conference_conference")) {
    override val single = {
        get[Long]("conference_from_id") ~ get[Long]("conference_to_id") ~ get[Long]("conference_relation_type_id") map {
            case conferenceFromId ~ conferenceToId ~ relationTypeId => {
                ConferenceConference(conferenceFromId, conferenceToId, relationTypeId)
            }
        }
    }

    def create(conferenceFromId: Long, conferenceToId: Long): Long = {
        createEntry('conference_from_id -> conferenceFromId, 'conference_to_id -> conferenceToId)
        createEntry('conference_from_id -> conferenceToId, 'conference_to_id -> conferenceFromId)
    }

    def create(conferenceFromId: Long, conferenceToId: Long, relationTypeId: Long): Long = {
        createEntry('conference_from_id -> conferenceFromId, 'conference_to_id -> conferenceToId, 'conferencerelationtype_id -> relationTypeId)
        createEntry('conference_from_id -> conferenceToId, 'conference_to_id -> conferenceFromId, 'conferencerelationtype_id -> relationTypeId)
    }
}