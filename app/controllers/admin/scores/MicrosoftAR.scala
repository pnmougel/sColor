package controllers.admin.scores

import scala.collection.mutable._
import controllers.admin._
import controllers.admin.action._
import models._
import play.api.mvc._
import controllers.utils.HtmlCache
import org.jsoup.nodes.Document
import collection.JavaConversions._

object MicrosoftAR extends AdminAction with PublicationCreator {

    case class MicrosoftConferenceInformation(override val name: String, override val shortName: Option[String],
                                              override val ctype: Option[CType] = None,
                                              override val field: Option[models.Field] = None,
                                              subFields: List[String],
                                              nbPublications: Int, hIndex: Int, url: String
                                               ) extends ConferenceInformation(name, shortName, ctype, field)

    case class MicrosoftARPublication(name: String, shortName: String, ctype: CType, field: Field)

    val description = "This action gather information from Microsoft Academic Research."

    val name = "microsoftAR"

    val label = "Microsoft Academic Research"

    override val category = "External Ranking"
    override val icon = "star-empty"

    val MicrosoftARJournalId = 4
    val MicrosoftARConferenceId = 3

    var curFieldId: Long = 0
    var conferenceToHindex = new Stack[(Long, Int)]()

    var nbObjectsTotal = 0
    var objectCounter = 0

    var publicationsT = new HashMap[String, MicrosoftConferenceInformation]()

    def updatePublicationInformation(publicationInformation: MicrosoftConferenceInformation) {
        publicationsT(publicationInformation.url) = {
            val pInfoOpt = publicationsT.get(publicationInformation.url)
            if (pInfoOpt.isDefined) {
                val pInfo = pInfoOpt.get
                MicrosoftConferenceInformation(pInfo.name,
                    pInfo.shortName, pInfo.ctype, pInfo.field,
                    publicationInformation.subFields ::: pInfo.subFields,
                    math.max(publicationInformation.nbPublications, pInfo.nbPublications),
                    math.max(publicationInformation.hIndex, pInfo.hIndex),
                    pInfo.url)
            } else {
                publicationInformation
            }
        }
    }

    override def conferenceCreated(publicationId: Long, conferenceInformation: ConferenceInformation) = {
        val information = conferenceInformation.asInstanceOf[MicrosoftConferenceInformation]
        Bibliometric.createOrUpdate(publicationId, "h-index", information.hIndex, BibliometricSource.microsoftARSource)
        Bibliometric.createOrUpdate(publicationId, "# articles", information.nbPublications, BibliometricSource.microsoftARSource)
        conferenceToHindex.push((publicationId, information.hIndex))
    }

    override def run(request: Request[AnyContent]) = {
        clearMessages()

        /*
          infoMessage("Parsing journals...")
          getByField(MicrosoftARJournalId)
         */
        infoMessage("Parsing conferences...")
        getByField(MicrosoftARConferenceId)

        infoMessage("Database inserts")
        var idx = 0;
        publicationsT.foreach {
            case (url, publicationInformation) =>
                setPercentage((100 * idx.toDouble / publicationsT.size).toInt)
                idx += 1
                PublicationMatching.addPublication(publicationInformation, this)
        }

        // Update the ranks from the hindex
        infoMessage("Updating ranks...")
        /*
        val conferenceRanks = Conference.getConferenceRankFromScore(conferenceToHindex)
        conferenceRanks.foreach { case ((conferenceId, scoreValue), score) =>
            ExternalScore.createOrUpdate(conferenceId, ExternalRanking.microsoftRankingId, score, "" + scoreValue)
        }
        */

        successMessage("Finished :)")
    }

    def getDocument(entityTypeId: Int, domain: Option[Int] = None, subDomain: Option[Int] = None, startPage: Option[Int] = None): Document = {
        val fileName = "items_type" + entityTypeId +
          (if (domain.isDefined) "_topDomain" + domain.get else "") +
          (if (subDomain.isDefined) "_subDomain" + subDomain.get else "") +
          (if (startPage.isDefined) "_start" + startPage.get else "")
        val url = "http://academic.research.microsoft.com/RankList?last=0&entitytype=" + entityTypeId +
          (if (domain.isDefined) "&topdomainid=" + domain.get else "") +
          (if (subDomain.isDefined) "&subdomainid=" + subDomain.get else "") +
          (if (startPage.isDefined) "&start=" + startPage.get + "&end=" + (startPage.get + 99) else "")
        HtmlCache.getDocument("microsoft_AR", fileName, url).get
    }


    def getTopDomains(entityTypeId: Int): HashMap[Int, String] = {
        val doc = getDocument(entityTypeId, Option(2))
        val topDomains = doc.select("div.option")
        var map = new HashMap[Int, String]()
        topDomains.foreach {
            topDomain =>
                map(topDomain.select("input").first().attr("value").toInt) = topDomain.select("span").first().text()
        }
        map
    }

    def getNbObjectByType(entityTypeId: Int): Int = {
        var nbObjects = 0
        getTopDomains(entityTypeId).foreach {
            field =>
                val topDomain = field._1
                val element = getDocument(entityTypeId, Option(topDomain)).getElementById("ctl00_MainContent_SearchSummary_lblResultMessage")
                if (element != null) {
                    val elementText = element.text().split(" of ")
                    if (elementText.size == 2) {
                        nbObjects += elementText(1).split(" ")(0).replaceAll(",", "").toInt
                    }
                }
        }
        nbObjects
    }

    def getSubDomains(entityTypeId: Int, topDomain: Int): HashMap[Int, String] = {
        val doc = getDocument(entityTypeId, Option(topDomain), Option(0))
        var found = false
        var map = new HashMap[Int, String]()
        doc.toString().split("\n").foreach {
            line =>
                if (line.contains("options[") && line.contains("] = {")) {
                    if (!found) {
                        found = true
                        line.split(";").foreach {
                            elem =>
                                val subFieldName = elem.split("\"")(3)
                                val subFieldId = elem.split("\"")(7)

                                // Handle special cases
                                if (subFieldName == "Last 5 Years") {
                                    map(0) = "Multidisciplinary"
                                } else {
                                    if (subFieldId != "0") {
                                        map(subFieldId.toInt) = subFieldName
                                    }
                                }
                        }
                    }
                }
        }
        map
    }

    def getByField(entityTypeId: Int) = {
        val entityTypeName = if (entityTypeId == MicrosoftARJournalId) "Journal" else "Conference"
        setPercentage(0)

        // Build percentage
        objectCounter = 0
        nbObjectsTotal = getNbObjectByType(entityTypeId)

        getTopDomains(entityTypeId).foreach {
            field =>

            // Add field into the table
                val fieldId = field._1
                val fieldName = FieldMapping.fieldMapping(field._2)

                curFieldId = models.Field.getOrCreate(fieldName)

                val map = getSubDomains(entityTypeId, field._1)
                map.foreach {
                    case (subDomainId, subDomainName) =>
                        getPage(entityTypeId, fieldId, subDomainId, subDomainName)
                }
        }
    }


    def getPage(entityTypeId: Int, topDomainId: Int, subDomainId: Int, subDomainName: String): Unit = {
        val doc = getDocument(entityTypeId, Option(topDomainId), Option(subDomainId))
        val results = doc.select("span.result").text()
        if (results.split(" ").size < 5) {
            return
        }
        val nbResults = results.replaceAll(",", "").split(" ")(4).toInt
        var startPage = 1
        var resultCount = 0
        while (startPage < nbResults + 1) {
            resultCount += getPublicationsInPage(entityTypeId, topDomainId, subDomainId, startPage, subDomainName)
            startPage += 100
        }
        if (resultCount != nbResults) {
            warningMessage("Found " + resultCount + " while " + nbResults + " where expected")
            warningMessage("http://academic.research.microsoft.com/RankList?entitytype=" + entityTypeId +
              "&topdomainid=" + topDomainId + "&subdomainid=" + subDomainId + "&last=0")
        }
    }


    def getPublicationsInPage(entityTypeId: Int, topDomainId: Int, subDomainId: Int, startPage: Int, subDomainName: String): Int = {
        val entityTypeName = if (entityTypeId == MicrosoftARJournalId) "Journal" else "Conference"
        val doc = getDocument(entityTypeId, Option(topDomainId), Option(subDomainId), Option(startPage))
        var nbPublicationsInPage = 0
        doc.select("tr").map {
            tr =>
                objectCounter += 1
                setPercentage((objectCounter * 100) / nbObjectsTotal)

                val url = "http://academic.research.microsoft.com" + tr.select("td.rank-content").select("a").attr("href")
                val name = tr.select("td.rank-content").select("a").text()
                val (shortName, fullName) = if (name.split(" - ").size != 2) (("", name)) else ((name.split(" - ")(0), name.split(" - ")(1)))
                val data = tr.select("td.staticOrderCol").text().split(" ")
                val (nbPublications, hIndex) = if (data.size == 2) (data(0), data(1)) else ("-1", "-1")
                if (data.size == 2) {
                    val ctype = if (entityTypeId == MicrosoftARConferenceId) CType.conferenceType else CType.journalType

                    val conferenceInformation = MicrosoftConferenceInformation(fullName, Option(shortName),
                        CType.byId(ctype), Field.byId(curFieldId), List(subDomainName), nbPublications.toInt, hIndex.toInt, url)
                    updatePublicationInformation(conferenceInformation)
                    nbPublicationsInPage += 1
                }
        }
        nbPublicationsInPage
    }
}