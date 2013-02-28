package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import controllers.AuthenticationActions._
import models._

object Publication extends Controller {
    val addPublicationForm = Form(
        tuple(
            "fullName" -> text,
            "shortName" -> text,
            "type" -> number,
            "field" -> number))

    val updatePublicationForm = Form(
        tuple(
            "id" -> number,
            "name" -> text,
            "shortName" -> text,
            "type" -> number,
            "region" -> number,
            "subFields" -> text,
            "startedOn" -> number,
            "publisher" -> text,
            "description" -> text))

    def publication(id: Long) = Authentication {
        implicit request =>
        // Retrieve the conference
            val conference = Conference.byId(id)

            if (conference.isDefined) {
                // val userVotes = UserVote.getUserVotesByConferenceId(id)

                // Update the scores
                // Should not be necessary
                Conference.updateUserScoreForConference(id)
                Conference.updateExternalScoreForConference(id)

                val vote = UserVote.userVoteForConference(id, request.user)
                Ok(views.html.conference(conference.get, vote, request.user))
            } else {
                Search.noResults(request)
            }
    }

    def publicationByName(name: String) = Action {
        implicit request =>
            val conference = Conference.findPublications(name)
            if (conference.size > 0) {
                Redirect(routes.Publication.publication(conference(0)._1.id))
            } else {
                Search.noResults(request)
            }
    }


    def editPublication(id: Long) = Authentication {
        implicit request =>
        // Retrieve the conference
            val conference = Conference.byId(id)

            if (conference.isDefined && request.user.isDefined) {
                Ok(views.html.conferences.edit(conference.get))
            } else {
                Redirect(routes.Publication.publication(id))
            }
    }

    def updatePublication() = Authentication {
        implicit request =>
            updatePublicationForm.bindFromRequest.fold(
                errors => BadRequest("Unable to update"),
                success = params => params match {
                    case (id, name, shortName, ctypeId, regionId, subFields, yearSince, publisher, description) => {
                        if (request.user.isDefined) {
                            val publisherId = if (publisher != "") Option(models.Publisher.getOrCreate(publisher)) else None
                            val yearSinceOpt: Option[Int] = if (yearSince == 0) None else Option(yearSince)
                            val descriptionOpt: Option[String] = if (description == "") None else Option(description)

                            /* Add and remove the subFields */
                            var subFieldsToRemove = List[Long]()
                            var subFieldsToAdd = List[Long]()

                            val newSubFieldsIds = if (subFields.isEmpty) Array[Int]() else subFields.split(",").map(_.toInt)
                            val currentSubFields = Conference.getSubFields(id).map(_.id)
                            newSubFieldsIds.foreach(subFieldId => if (!currentSubFields.contains(subFieldId)) {
                                subFieldsToAdd = subFieldId :: subFieldsToAdd
                            })
                            currentSubFields.foreach {
                                subFieldId =>
                                    if (!newSubFieldsIds.contains(subFieldId)) {
                                        subFieldsToRemove = subFieldId :: subFieldsToRemove
                                    }
                            }
                            Conference.update(id, name, shortName, ctypeId, regionId, yearSinceOpt, publisherId, descriptionOpt,
                                subFieldsToAdd, subFieldsToRemove, request.user.get.id)
                        }

                        Redirect(routes.Publication.publication(id))
                    }
                }
            )
    }

    def getHistory(action: String, conferenceId: Long) = Action {
        implicit request =>
            val conference = Conference.byId(conferenceId)
            if (conference.isDefined) {
                if (action == "all") {
                    Ok(views.html.snippets.update.updates(conference.get.updatesAll))
                } else {
                    Ok(views.html.snippets.update.updates(conference.get.updates))
                }
            } else {
                Ok
            }
    }

    def addPublication() = Authentication {
        implicit request =>
            addPublicationForm.bindFromRequest.fold(
                errors => BadRequest("Unable to create"),
                params => params match {
                    case (fullName, shortName, cType, field) =>
                        if (request.user.isDefined) {
                            val newConferenceId = Conference.create(fullName, shortName, cType, field, userIdOpt = Option(request.user.get.id))
                            Redirect(routes.Publication.publication(newConferenceId))
                        } else {
                            Ok
                        }
                }
            )
    }

}