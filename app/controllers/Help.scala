package controllers

import play.api.mvc._
import org.apache.commons.lang3.text.WordUtils

object Help extends Controller {

    val menuSections = List("About" -> List(),
        "Account" -> List("Create an account", "Update your profile"),
        "Contribute" -> List("Vote", "Edit publication", "Idea box"),
        "Scores" -> List("User scores", "External scores", "Core", "Google Scholar", "Microsoft Academic Research"),
        "API" -> List("Access", "Basic usage", "Examples"),
        "Contact us" -> List(),
        "Privacy" -> List())

    def nametoLink(name: String) = name.toLowerCase().replaceAll(" ", "_")

    def linktoName(link: String) = {
        if (link == "api") {
            "API"
        } else {
            WordUtils.capitalizeFully(link.replace("_", " "), '#')
        }
    }

    def section(name: String) = Action {
        implicit request =>
            val lowerCaseName = nametoLink(name)
            if (menuSections.exists {
                case (section, subsections) => {
                    lowerCaseName == nametoLink(section) || subsections.exists(lowerCaseName == nametoLink(_))
                }
            }) {
                Ok(views.html.help.mainHelp(request, lowerCaseName))
            } else {
                Ok(views.html.help.mainHelp(request, "about"))
            }
    }
}