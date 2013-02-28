package controllers.admin.scores

import java.io.File
import scala.collection.immutable.HashMap
import scala.io.Source.fromFile
import controllers.admin.action.AdminAction
import models.CType
import controllers.admin.FieldMapping
import models.Conference
import models.ExternalRanking
import models.ExternalScore
import models.SubField
import models.Field
import play.api.mvc.AnyContent
import play.api.mvc.Request

object Core extends AdminAction with PublicationCreator {

    case class CorePublicationInformation(override val name: String, override val shortName: Option[String],
                                          override val ctype: Option[CType] = None,
                                          override val field: Option[models.Field] = None,
                                          scoreText: String,
                                          subFields: List[String]) extends ConferenceInformation(name, shortName, ctype, field)


    val description = "This action gather the rankings from Core."

    val name = "core"

    val label = "Core"

    override val category = "External Ranking"
    override val icon = "star-empty"

    override def conferenceCreated(publicationId: Long, conferenceInformation: ConferenceInformation) {
        val information = conferenceInformation.asInstanceOf[CorePublicationInformation]
        val score: Double = information.scoreText match {
            case "A*" => 5
            case "A" => 4.75
            case "B" => 4
            case "C" => 2.75
            case _ => -1.0
        }

        val rankingId = if (information.ctype.get.id == CType.conferenceType) ExternalRanking.coreConferenceRankingId else ExternalRanking.coreJournalRankingId
        if (score != -1) {
            ExternalScore.createOrUpdate(publicationId, rankingId, score, information.scoreText)
        }
        information.subFields.foreach {
            subFields =>
                val subFieldId = SubField.getOrCreate(subFields, information.field.get.id)
                Conference.byId(publicationId).get.addSubField(subFieldId)
        }
    }

    val fieldMatching = HashMap(
        "01" -> Field.getOrCreate(FieldMapping.mathematics),
        "02" -> Field.getOrCreate(FieldMapping.physics),
        "03" -> Field.getOrCreate(FieldMapping.chemistry),
        "04" -> Field.getOrCreate(FieldMapping.earthSciences),
        "05" -> Field.getOrCreate(FieldMapping.environmentalSciences),
        "06" -> Field.getOrCreate(FieldMapping.earthSciences),
        "07" -> Field.getOrCreate(FieldMapping.agricultureAndVeterinary),
        "08" -> Field.getOrCreate(FieldMapping.computerScience),
        "09" -> Field.getOrCreate(FieldMapping.engineering),
        "10" -> Field.getOrCreate(FieldMapping.technology),
        "11" -> Field.getOrCreate(FieldMapping.medecineAndHealth),
        "12" -> Field.getOrCreate(FieldMapping.architecture),
        "13" -> Field.getOrCreate(FieldMapping.education),
        "14" -> Field.getOrCreate(FieldMapping.economicsAndBusiness),
        "15" -> Field.getOrCreate(FieldMapping.economicsAndBusiness),
        "16" -> Field.getOrCreate(FieldMapping.humanities),
        "17" -> Field.getOrCreate(FieldMapping.psychology),
        "18" -> Field.getOrCreate(FieldMapping.legalStudies),
        "19" -> Field.getOrCreate(FieldMapping.artsAndHumanities),
        "20" -> Field.getOrCreate(FieldMapping.languageAndCulture),
        "21" -> Field.getOrCreate(FieldMapping.historyAndArchaeology),
        "22" -> Field.getOrCreate(FieldMapping.philosophyAndReligions),
        "MD" -> Field.getOrCreate(FieldMapping.multidisciplinary))

    def getField(forCode: String): Long = {
        fieldMatching(forCode.substring(0, 2))
    }

    case class MatchingPublication()

    val matchingPublication = new HashMap[Int, MatchingPublication]()

    override def run(request: Request[AnyContent]) {
        // clearMessages()

        infoMessage("Remove previous core ranking...")
        ExternalScore.deleteByExternalRanking(ExternalRanking.coreConferenceRankingId)
        ExternalScore.deleteByExternalRanking(ExternalRanking.coreJournalRankingId)

        infoMessage("Inserting Core Ranking...")

        // Conferences
        infoMessage("Adding conferences...")
        val conferenceType = CType.getOrCreate("Conference")
        val conferenceFile = new File("cache/core/core_2010_conference.csv")
        val nbLines = fromFile(conferenceFile).getLines().size
        fromFile(conferenceFile).getLines().zipWithIndex foreach {
            case (line, idx) =>
                setPercentage(((idx + 1) * 100 / nbLines))
                val elems = line.split("\\|")
                if (elems.size >= 6 && idx != 0) {
                    val name = elems(1)
                    val shortName = elems(2)
                    val scoreText = elems(3)
                    val fieldId = getField(elems(4))

                    var subfields = List[String]()
                    if (elems(4).size == 4) {
                        subfields = elems(5) :: subfields
                    }
                    if (elems.size >= 8 && !elems(6).isEmpty) {
                        if (getField(elems(6)) == fieldId && elems(6).size == 4) {
                            subfields = elems(7) :: subfields
                        }
                    }
                    if (elems.size >= 10 && !elems(8).isEmpty) {
                        if (getField(elems(8)) == fieldId && elems(8).size == 4) {
                            subfields = elems(9) :: subfields
                        }
                    }
                    addConference(name, shortName, conferenceType, fieldId, scoreText, subfields)
                }
        }

        // Journals
        infoMessage("Adding journals...")

        val journalType = CType.getOrCreate("Journal")
        val journalFile = new File("cache/core/core_2010_journal.csv")
        val nbJournals = fromFile(journalFile).getLines().size
        fromFile(journalFile).getLines().zipWithIndex foreach {
            case (line, idx) =>
                setPercentage(((idx + 1) * 100 / nbJournals))
                val elems = line.split("\\|")
                if (elems.size >= 6 && idx != 0) {
                    val scoreText = elems(1)
                    val name = elems(2)
                    val shortName = ""
                    val fieldId = getField(elems(3))
                    var subFields = List[String]()
                    if (elems(3).size == 3) {
                        subFields = elems(4) :: subFields
                    }
                    if (elems.size >= 7 && !elems(5).isEmpty) {
                        if (getField(elems(5)) == fieldId && elems(5).size == 4) {
                            subFields = elems(6) :: subFields
                        }
                    }
                    if (elems.size >= 9 && !elems(7).isEmpty) {
                        if (getField(elems(7)) == fieldId && elems(7).size == 4) {
                            subFields = elems(8) :: subFields
                        }
                    }

                    addConference(name, shortName, journalType, fieldId, scoreText, subFields)
                }
        }
        infoMessage("Done")
    }

    def addConference(name: String, shortName: String, ctypeId: Long, fieldId: Long, scoreText: String, subFields: List[String]) {
        val conferenceInformation = CorePublicationInformation(name, Option(shortName),
            CType.byId(ctypeId), Field.byId(fieldId), scoreText, subFields)
        PublicationMatching.addPublication(conferenceInformation, this)
    }
}