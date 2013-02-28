package models

import anorm._
import anorm.SqlParser._
import play.api.Play.current
import play.api.db._
import models.renorm._
import controllers.utils.Utils
import java.util.Date
import collection.mutable

case class Conference(name: String, shortName: String,
                      yearSince: Option[Int], description: Option[String],
                      externalScore: Option[Double], userScore: Option[Double], avgScore: Option[Double],
                      fieldId: Long, publisherId: Option[Long], ctypeId: Long, regionId: Long) {

    var id: Long = 0

    lazy val field = Field.byId(fieldId).get
    lazy val ctype = CType.byId(ctypeId).get
    lazy val publisher = if (publisherId.isDefined) Publisher.byId(publisherId.get) else None
    lazy val bibliometrics = Bibliometric.find('conference_id -> id)

    lazy val relatedConferences = DB.withConnection {
        implicit c => SQL( """
                SELECT conference.* FROM conference, Conference_Conference 
                WHERE conference_from_id = {id} AND conference_to_id = conference.id
                           """).on('id -> id).as(Conference.single *)
    }
    lazy val comments = Comment.select(Where(Seq('conference_id -> id)) :: OrderBy("created_at", "DESC"))
    lazy val links = Link.find('conference_id -> id)
    lazy val externalScores = ExternalScore.find('conference_id -> id)
    lazy val userScores = UserVote.find('conference_id -> id)
    lazy val userVotes = UserVote.getUserVotesByConferenceId(id)

    // Subfields
    lazy val subfields = Conference.getSubFields(id)

    def addSubField(subfieldId: Long) = DB.withConnection {
        implicit c =>
            val isAlreadyPresent = SQL("SELECT count(*) FROM conference_subfield WHERE conference_id = {conferenceId} and subfield_id = {subfieldId}").on(
                'conferenceId -> id, 'subfieldId -> subfieldId).as(scalar[Long].single)
            if (isAlreadyPresent == 0) {
                SQL("INSERT INTO conference_subfield (conference_id, subfield_id) VALUES ({conferenceId}, {subfieldId})").on(
                    'conferenceId -> id, 'subfieldId -> subfieldId).executeUpdate()
            }
    }

    def removeSubField(subfieldId: Long) = DB.withConnection {
        implicit c =>
            SQL("DELETE FROM conference_subfield WHERE conference_id = {conferenceId} AND subfield_id = {subfieldId}").on(
                'conferenceId -> id, 'subfieldId -> subfieldId).executeUpdate()
    }

    lazy val updates = Update.select(Where(Seq('conference_id -> id)) :: OrderBy("created_at", "DESC") :: Limit(Update.defaultHistorySize))
    lazy val updatesAll = Update.select(Where(Seq('conference_id -> id)) :: OrderBy("created_at", "DESC"))
}

object Conference extends Table[Conference] {
    def countPages(fieldId: Long, ctypes: List[Long], subFields: List[Long]): Long = DB.withConnection {
        implicit c =>
            val subFieldsStr: String = if (subFields.size > 0) subFields.mkString("(", ",", ")") else " (NULL) "
            val ctypesStr: String = if (ctypes.size > 0) ctypes.mkString("(", ",", ")") else " (NULL) "
            SQL(
                "SELECT COUNT(conference.*) " +
                  "FROM conference, conference_subfield " +
                  "WHERE conference.ctype_id IN " + ctypesStr +
                  "AND conference.field_id = {fieldId} " +
                  "AND conference_subfield.subfield_id IN " + subFieldsStr +
                  "AND conference_subfield.conference_id = conference.id").on(
                'fieldId -> fieldId).as(scalar[Long].single)
    }

    def getPage(fieldId: Long, ctypes: List[Long], subFields: List[Long], nbConferencesPerPage: Option[Int], startAt: Option[Int], orderBy: String, sort: String): List[Conference] = DB.withConnection {
        implicit c =>
            val subFieldsStr: String = if (subFields.size > 0) subFields.mkString("(", ",", ")") else " (NULL) "
            val ctypesStr: String = if (ctypes.size > 0) ctypes.mkString("(", ",", ")") else " (NULL) "
            var params: List[(String, ParameterValue[_])] = List("fieldId" -> fieldId, "nbConferencesPerPage" -> nbConferencesPerPage, "sort" -> sort)
            var sqlLimit = ""
            if (nbConferencesPerPage.isDefined) {
                params = params ::: List[(String, ParameterValue[_])]("nbConferencesPerPage" -> nbConferencesPerPage, "startAt" -> startAt)
                sqlLimit = "LIMIT {nbConferencesPerPage} OFFSET {startAt} "
            }
            val orderByQuery = "ORDER BY " + orderBy + " " + sort + " NULLS LAST, " + List("avg_score", "user_score", "external_score").
              filter(_ != orderBy).
              map(_ + " " + sort + " NULLS LAST ").
              mkString(", ")
            SQL("SELECT conference.* " +
              "FROM conference, conference_subfield " +
              "WHERE conference.ctype_id IN " + ctypesStr +
              "AND conference.field_id = {fieldId} " +
              "AND conference_subfield.subfield_id IN " + subFieldsStr +
              "AND conference_subfield.conference_id = conference.id " +
              orderByQuery + ", name ASC, short_name ASC " + sqlLimit).on(params.toArray: _*).as(single *)
    }

    /*
     * Subfields
     */
    /*
    // Add a subfield to the conference
    def addSubField(conferenceId: Long, subfieldId: Long) = DB.withConnection { implicit c =>
        val isAlreadyPresent = SQL("SELECT count(*) FROM conference_subfield WHERE conference_id = {conferenceId} and subfield_id = {subfieldId}").on(
            'conferenceId -> conferenceId, 'subfieldId -> subfieldId).as(scalar[Long].single)
        if (isAlreadyPresent == 0) {
            SQL("INSERT INTO conference_subfield (conference_id, subfield_id) VALUES ({conferenceId}, {subfieldId})").on(
                'conferenceId -> conferenceId, 'subfieldId -> subfieldId).executeUpdate()
        }
    }

    // Remove a subfield
    def removeSubField(conferenceId: Long, subfieldId: Long) = DB.withConnection { implicit c =>
        SQL("DELETE FROM conference_subfield WHERE conference_id = {conferenceId} AND subfield_id = {subfieldId}").on(
            'conferenceId -> conferenceId, 'subfieldId -> subfieldId).executeUpdate()
    }
     */

    // List the subFields related to the conference
    def getSubFields(conferenceId: Long): List[SubField] = DB.withConnection {
        implicit c =>
            SQL("SELECT subfield.* FROM subfield, conference_subfield " +
              "WHERE conference_subfield.conference_id = {conferenceId} AND conference_subfield.subfield_id = subfield.id").on(
                'conferenceId -> conferenceId).as(SubField.single *)
    }

    def countPublications(query: String, shortName: Option[String] = None, ctypeId: Option[Long] = None, fieldId: Option[Long] = None) = DB.withConnection {
        implicit c =>
            val queryLower = query.toLowerCase
            SQL("SELECT COUNT(*) FROM conference " + buildWhereQuery("LIKE", shortName, ctypeId, fieldId)).on(
                'query -> ("%" + queryLower + "%"),
                'fieldId -> fieldId.getOrElse(0),
                'ctypeId -> ctypeId.getOrElse(0),
                'shortName -> shortName.getOrElse("")
            ).as(scalar[Long].single)
    }

    /**
     * Default search query 
     */
    private def buildWhereQuery(test: String, shortName: Option[String] = None, ctypeId: Option[Long] = None, fieldId: Option[Long] = None): String = {
        var filter = " WHERE (name_lower " + test + " {query} OR short_name_lower " + test + " {query}) "
        if (ctypeId.isDefined) {
            filter += " AND ctype_id = {cTypeId} "
        }
        if (fieldId.isDefined) {
            filter += " AND field_id = {fieldId} "
        }
        if (shortName.isDefined) {
            filter += " AND short_name_lower = {shortName} "
        }
        filter
    }

    def findPublications(query: String, shortName: Option[String] = None, ctypeId: Option[Long] = None,
                         fieldId: Option[Long] = None, allResults: Boolean = false): List[(Conference, Double)] = DB.withConnection {
        implicit c =>
            val queryLower = query.toLowerCase
            var publications = SQL("SELECT * FROM conference " + buildWhereQuery("=", shortName, ctypeId, fieldId)).on(
                'query -> queryLower,
                'fieldId -> fieldId.getOrElse(0),
                'ctypeId -> ctypeId.getOrElse(0),
                'shortName -> shortName.getOrElse("")
            ).as(single *).zip(Stream.continually(1D))
            if (publications.isEmpty || allResults) {
                publications = publications ::: SQL("SELECT * FROM conference " + buildWhereQuery("LIKE", shortName, ctypeId, fieldId) + " LIMIT 200").on(
                    'query -> ("%" + queryLower + "%"),
                    'fieldId -> fieldId.getOrElse(0),
                    'ctypeId -> ctypeId.getOrElse(0),
                    'shortName -> shortName.getOrElse("")
                ).as(single *).zip(Stream.continually(1D))
            }
            if (publications.isEmpty || allResults) {
                // Perform search using stems
                val stems = new mutable.HashSet[String]()
                Utils.cleanName(query + " " + shortName).split(" ").foreach {
                    stems.add(_)
                }
                publications = publications ::: Stem.getMatchingConferences(stems).map {
                    case (conferenceId, matchingScore) =>
                        (Conference.byId(conferenceId).get, matchingScore)
                }
            }
            publications
    }

    def findByName(query: String, ctypeId: Option[Long] = None,
                   fieldId: Option[Long] = None): Option[Conference] = DB.withConnection {
        implicit c =>
            SQL("SELECT * FROM conference " + buildWhereQuery("=", None, ctypeId, fieldId)).on(
                'query -> query.toLowerCase,
                'fieldId -> fieldId.getOrElse(0),
                'ctypeId -> ctypeId.getOrElse(0),
                'shortName -> ""
            ).as(single.singleOpt)
    }

    /**
     * Query used to retrieve typeahead matches
     */
    def getQuickMatch(query: String): List[Conference] = DB.withConnection {
        implicit c =>
            SQL( """
            SELECT * FROM conference WHERE name_lower LIKE {query} OR short_name_lower LIKE {query}
                 			""").on('query -> query.toLowerCase).as(single *)
    }

    def findByField(fieldId: Long): List[Conference] = DB.withConnection {
        implicit c =>
            SQL("SELECT * FROM Conference WHERE field_id = {fieldId} ORDER BY external_score DESC NULLS LAST LIMIT 10").on(
                'fieldId -> fieldId).as(single *)
    }

    /**
     * INSERT
     */
    def getOrCreate(name: String, shortName: String, ctypeId: Long, fieldId: Long, regionId: Option[Long] = None): (Long, Boolean) = DB.withConnection {
        implicit c =>
            val conference = findOption('name_lower -> name.toLowerCase, 'short_name_lower -> shortName.toLowerCase,
                'ctype_id -> ctypeId, 'field_id -> fieldId)
            if (conference.isDefined) {
                (conference.get.id, false)
            } else {
                (create(name, shortName, ctypeId, fieldId, regionId), true)
            }
    }

    def create(name: String, shortName: String, ctypeId: Long, fieldId: Long, regionIdOpt: Option[Long] = None, userIdOpt: Option[Long] = None): Long = {
        val regionId = if (regionIdOpt.isDefined) regionIdOpt.get else Region.getOrCreate("Worldwide", None, isInternational = true)
        val userId = if (userIdOpt.isDefined) userIdOpt.get else User.findByEmail("admin").get.id
        val publicationId = createEntry('name -> name, 'short_name -> shortName, 'ctype_id -> ctypeId, 'field_id -> fieldId, 'region_id -> regionId,
            'name_lower -> name.toLowerCase, 'short_name_lower -> shortName.toLowerCase,
            'created_at -> new Date(), 'user_id -> userId)

        // Add the stems
        Stem.addStemForConference(name + " " + shortName, publicationId)
        publicationId
    }

    /**
     * UPDATE
     */
    def update(id: Long, name: String, shortName: String, cTypeId: Long, regionId: Long, yearSince: Option[Int], publisherId: Option[Long],
               description: Option[String], subFieldsToAdd: List[Long], subFieldsToRemove: List[Long], userId: Long) = DB.withConnection {
        implicit c =>
            val conference = byId(id).get

            // If a change occurred, record the update
            if (name != conference.name || shortName != conference.shortName || description != conference.description || cTypeId != conference.ctypeId ||
              regionId != conference.regionId || yearSince != conference.yearSince || publisherId != conference.publisherId ||
              !subFieldsToAdd.isEmpty || !subFieldsToRemove.isEmpty) {
                val updateId = Update.create(id, userId)
                if (name != conference.name) {
                    UpdateName.create(updateId, conference.name, name)
                }
                if (shortName != conference.shortName) {
                    UpdateShortname.create(updateId, conference.shortName, shortName)
                }
                if (description != conference.description) {
                    UpdateDescription.create(updateId, conference.description, description)
                }
                if (cTypeId != conference.ctypeId) {
                    UpdateCtype.create(updateId, conference.ctypeId, cTypeId)
                }
                if (regionId != conference.regionId) {
                    UpdateRegion.create(updateId, conference.regionId, regionId)
                }
                if (yearSince != conference.yearSince) {
                    val yearSinceBefore = if (conference.yearSince.isDefined) conference.yearSince.get else 0
                    val yearSinceAfter = if (yearSince.isDefined) yearSince.get else 0
                    UpdateCreationDate.create(updateId, yearSinceBefore, yearSinceAfter)
                }
                if (publisherId != conference.publisherId) {
                    UpdatePublisher.create(updateId, conference.publisherId, publisherId)
                }
                subFieldsToAdd.foreach {
                    subFieldId =>
                        val isAlreadyPresent = SQL("SELECT count(*) FROM conference_subfield WHERE conference_id = {conferenceId} and subfield_id = {subFieldId}").on(
                            'conferenceId -> id, 'subfieldId -> subFieldId).as(scalar[Long].single)
                        if (isAlreadyPresent == 0) {
                            SQL("INSERT INTO conference_subfield (conference_id, subfield_id) VALUES ({conferenceId}, {subFieldId})").on(
                                'conferenceId -> id, 'subfieldId -> subFieldId).executeUpdate()
                        }
                        UpdateAddSubfield.create(updateId, subFieldId)
                }

                subFieldsToRemove.foreach {
                    subfieldId =>
                        SQL("DELETE FROM conference_subfield WHERE conference_id = {conferenceId} AND subfield_id = {subfieldId}").on(
                            'conferenceId -> id, 'subfieldId -> subfieldId).executeUpdate()
                        UpdateRemoveSubfield.create(updateId, subfieldId)
                }

                super.update(id, 'name -> name, 'short_name -> shortName, 'ctype_id -> cTypeId, 'region_id -> regionId,
                    'year_since -> yearSince, 'publisher_id -> publisherId, 'description -> description)
            }
    }

    def getConferenceRankFromScore(scores: mutable.HashMap[Conference, Int]): mutable.HashMap[Conference, (Int, Double)] = {
        // Group the conference by subFields
        val subFieldToConferences = new mutable.HashMap[SubField, mutable.Stack[(Conference, Int)]]()
        scores.foreach {
            case (conference, scoreValue) =>
                val subFields = conference.subfields
                subFields.foreach {
                    subField =>
                        subFieldToConferences.getOrElseUpdate(subField, new mutable.Stack[(Conference, Int)]()).push((conference, scoreValue))
                }
        }

        // Compute the score
        val conferenceRanks = new mutable.HashMap[Conference, (Int, Double)]()
        subFieldToConferences.foreach {
            case (subField, confList) =>
                val nbConferenceInSubField = subField.countConferencesWithSubfield
                val delta: Double = 5D / nbConferenceInSubField
                confList.sortWith(_._2 > _._2).zipWithIndex.foreach {
                    case ((conference, score), idx) =>
                        val scoreValue: Double = 5D - (idx * delta)
                        val curScoreValue = conferenceRanks.getOrElse(conference, (score, 0D))._2
                        conferenceRanks(conference) = (score, math.max(curScoreValue, scoreValue))
                }
        }
        conferenceRanks
    }

    def updateRegion(whereCondition: String, regionId: Long) = DB.withConnection {
        implicit c =>
            SQL("UPDATE conference SET region_id = {regionId} WHERE name ILIKE {whereCondition} OR short_name ILIKE {whereCondition}").on(
                'whereCondition -> whereCondition,
                'regionId -> regionId).executeUpdate()
    }

    def updateType(whereCondition: String, ctypeId: Long) = DB.withConnection {
        implicit c =>
            SQL("UPDATE conference SET ctype_id = {cTypeId} WHERE name ILIKE {whereCondition} OR short_name ILIKE {whereCondition}").on(
                'whereCondition -> whereCondition,
                'ctypeId -> ctypeId).executeUpdate()
    }

    def updatePublisher(whereCondition: String, publisherId: Long) = DB.withConnection {
        implicit c =>
            SQL("UPDATE conference SET publisher_id = {publisherId} WHERE name ILIKE {whereCondition} OR short_name ILIKE {whereCondition}").on(
                'whereCondition -> whereCondition,
                'publisherId -> publisherId).executeUpdate()
    }

    def updateUserScoreForConference(conferenceId: Long) = DB.withConnection {
        implicit c =>
            val userScore = UserVote.getUserVotesByConferenceId(conferenceId)
            val score = if (userScore.avgUserScore == -1) None else Option[Double](userScore.avgUserScore)
            update(conferenceId, 'user_score -> score)
            updateAvgScoreForConference(conferenceId)
    }

    def updateExternalScoreForConference(conferenceId: Long) = DB.withConnection {
        implicit c =>
            var externalScoreTotal: Double = 0
            val externalScores = ExternalScore.getByConferenceId(conferenceId)
            externalScores.foreach {
                externalScore =>
                    externalScoreTotal += externalScore.score
            }
            val externalScore: Option[Double] = if (externalScores.size > 0) Option[Double](externalScoreTotal / externalScores.size) else None
            update(conferenceId, 'external_score -> externalScore)
            updateAvgScoreForConference(conferenceId)
    }

    def updateAvgScoreForConference(conferenceId: Long) = DB.withConnection {
        implicit c =>
            val conferenceOpt = byId(conferenceId)
            if (conferenceOpt.isDefined) {
                val conference = conferenceOpt.get
                val avgScore = if (conference.userScore.isDefined && conference.externalScore.isDefined) {
                    Option((conference.userScore.get + conference.externalScore.get) / 2)
                } else if (conference.userScore.isDefined) {
                    conference.userScore
                } else if (conference.externalScore.isDefined) {
                    conference.externalScore
                } else {
                    None
                }
                update(conferenceId, 'avg_score -> avgScore)
            }
    }
}