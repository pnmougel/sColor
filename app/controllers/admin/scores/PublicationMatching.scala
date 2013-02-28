package controllers.admin.scores

import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import models._
import controllers.admin._
import controllers.admin.action._
import scala.collection.mutable.HashMap

trait PublicationCreator extends AdminAction {
    def conferenceCreated(conferenceId: Long, conferenceInformation: ConferenceInformation)
}

abstract class ConferenceInformation(val name: String, val shortName: Option[String], val ctype: Option[CType] = None,
                                     val field: Option[models.Field] = None)

object PublicationMatching extends Controller {
    var matchingPublicationsFound = new HashMap[Int, (PublicationCreator, ConferenceInformation)]
    var curMatchingId = 0

    val mergeForm = Form(
        tuple(
            "mergeId" -> number,
            "publicationId" -> number
        ))
    val createForm = Form("mergeId" -> number)

    def addPublication(conferenceInformation: ConferenceInformation, publicationCreator: PublicationCreator) = {
        val ctypeId = if (conferenceInformation.ctype.isDefined) Option(conferenceInformation.ctype.get.id) else None
        val fieldId = if (conferenceInformation.field.isDefined) Option(conferenceInformation.field.get.id) else None

        // Special case in microsoft AR. Publication names formed by a single word are usually highly generic words matching
        // many other publications. 
        // If the exact name is not already present, we create the new publication.
        if (publicationCreator == MicrosoftAR && !conferenceInformation.name.contains(" ")) {
            val publication = Conference.findByName(conferenceInformation.name, ctypeId, fieldId)
            val publicationId = if (publication.isDefined) {
                publication.get.id
            } else {
                Conference.create(conferenceInformation.name, conferenceInformation.shortName.get, ctypeId.get, fieldId.get)
            }
            publicationCreator.conferenceCreated(publicationId, conferenceInformation)
        } else if (publicationCreator == Core) {
            // For core we always create the publication
            val publicationId = Conference.create(conferenceInformation.name, conferenceInformation.shortName.get, ctypeId.get, fieldId.get)
            publicationCreator.conferenceCreated(publicationId, conferenceInformation)
        } else {
            val matchingPublications = Conference.findPublications(conferenceInformation.name,
                conferenceInformation.shortName, ctypeId, fieldId)
            if (matchingPublications.size == 0) {
                // Create a new entry if field and type are defined
                if (ctypeId.isDefined && fieldId.isDefined) {
                    val conferenceId = Conference.create(conferenceInformation.name, conferenceInformation.shortName.get,
                        ctypeId.get, fieldId.get)
                    publicationCreator.conferenceCreated(conferenceId, conferenceInformation)
                }
            } else if (matchingPublications.size == 1) {
                // Use directly the conference matched ?
                // Well the user might want to select fields...
                val matchingPublication = matchingPublications(0)._1
                if (matchingPublication.name == conferenceInformation.name) {
                    publicationCreator.conferenceCreated(matchingPublications(0)._1.id, conferenceInformation)
                } else {
                    matchingPublicationsFound(curMatchingId) = (publicationCreator, conferenceInformation)
                    ClientCommunication.addHtml(publicationCreator.name, views.html.admin.snippets.mergeConference(curMatchingId, conferenceInformation, matchingPublications))
                    curMatchingId += 1
                }
            } else {
                matchingPublicationsFound(curMatchingId) = (publicationCreator, conferenceInformation)
                ClientCommunication.addHtml(publicationCreator.name, views.html.admin.snippets.mergeConference(curMatchingId, conferenceInformation, matchingPublications))
                curMatchingId += 1
            }
        }
    }

    def sameAsPublication() = Action {
        implicit request =>
            val (mergeId, publicationId) = mergeForm.bindFromRequest.get
            val (publicationCreator, conferenceInformation) = matchingPublicationsFound(mergeId)
            publicationCreator.conferenceCreated(publicationId, conferenceInformation)
            Ok("")
    }

    def createPublication() = Action {
        implicit request =>
            val (mergeId) = createForm.bindFromRequest.get
            val (publicationCreator, conferenceInformation) = matchingPublicationsFound(mergeId)

            val ctypeId = if (conferenceInformation.ctype.isDefined) Option(conferenceInformation.ctype.get.id) else None
            val fieldId = if (conferenceInformation.field.isDefined) Option(conferenceInformation.field.get.id) else None
            if (ctypeId.isDefined && fieldId.isDefined) {
                val conferenceId = Conference.create(conferenceInformation.name, conferenceInformation.shortName.get,
                    ctypeId.get, fieldId.get)
                publicationCreator.conferenceCreated(conferenceId, conferenceInformation)
            }
            Ok("")
    }
}