package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models.Conference
import play.api.libs.json.Json
import collection.mutable

object Search extends Controller {
    val searchForm = Form(tuple(
        "query" -> text,
        "shortName" -> text,
        "type" -> number,
        "field" -> number))
    val simpleQuery = Form("query" -> text)

    def search = Action {
        implicit request =>
            searchForm.bindFromRequest.fold(
                errors => BadRequest("How did you manage that ?"),
                query => query match {
                    case (name, shortName, ctypeId, fieldId) =>
                        val shortNameOpt = if (shortName.isEmpty) None else Option(shortName)
                        val ctypeIdOpt: Option[Long] = if (ctypeId == -1) None else Option(ctypeId)
                        val fieldIdOpt: Option[Long] = if (fieldId == -1) None else Option(fieldId)

                        val conferences = Conference.findPublications(name, shortNameOpt, ctypeIdOpt, fieldIdOpt)
                        val nbResults = if (conferences.size == 200) {
                            Conference.countPublications(name, shortNameOpt, ctypeIdOpt, fieldIdOpt)
                        } else {
                            conferences.size
                        }
                        if (nbResults == 1) {
                            // Only one conference is matching
                            Redirect(routes.Publication.publication(conferences(0)._1.id))
                        } else {
                            // Display the list of all matching conferences
                            val matchingExactly = conferences.filter {
                                case (conference, scoreMatching) =>
                                    conference.name.equalsIgnoreCase(name) ||
                                      conference.shortName.equalsIgnoreCase(name)
                            }
                            if (matchingExactly.size == 1) {
                                Redirect(routes.Publication.publication(matchingExactly(0)._1.id))
                            } else {
                                Ok(views.html.results(name, conferences, nbResults))
                            }
                        }
                }
            )
    }

    def json = Action {
        implicit request =>
            simpleQuery.bindFromRequest.fold(
                errors => BadRequest("How did you manage that ?"),
                query => {
                    val lowerCaseQuery = query.toLowerCase
                    val matchingPublicationsSet = new mutable.HashMap[String, Int]()
                    Conference.getQuickMatch("%" + lowerCaseQuery + "%").map {
                        publication =>
                            val shortNameLower = publication.shortName.toLowerCase
                            val nameLower = publication.name.toLowerCase
                            val displayedName = publication.name + (if (shortNameLower.trim.size != 0) {
                                " (" + publication.shortName + ")"
                            } else {
                                ""
                            })
                            // Score the matching
                            if (shortNameLower == lowerCaseQuery) {
                                matchingPublicationsSet(displayedName) = 1
                            } else if (nameLower == lowerCaseQuery) {
                                matchingPublicationsSet(displayedName) = 2
                            } else if (shortNameLower.startsWith(lowerCaseQuery)) {
                                matchingPublicationsSet(displayedName) = 3
                            } else if (nameLower.startsWith(lowerCaseQuery)) {
                                matchingPublicationsSet(displayedName) = 4
                            } else if (shortNameLower.endsWith(lowerCaseQuery)) {
                                matchingPublicationsSet(displayedName) = 5
                            } else if (nameLower.endsWith(lowerCaseQuery)) {
                                matchingPublicationsSet(displayedName) = 6
                            } else {
                                matchingPublicationsSet(displayedName) = 7
                            }
                    }
                    val sortedPublications = matchingPublicationsSet.toList.sortBy(_._2).take(8).map(p => p._1 + " " + p._2)
                    Ok(Json.toJson(Map("options" -> sortedPublications)))
                    /*
                  if(sortedPublications.size == 1) {
                    Ok("")
                  } else {
                    Ok(Json.toJson(Map("options" -> sortedPublications)))
                  }
                  */
                }
            )


    }

    def noResults = Action {
        implicit request =>
            Ok(views.html.results("", List(), 0L))
    }
}