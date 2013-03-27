package controllers.admin

import scala.collection.mutable._
import play.api.mvc._
import play.api.templates.Html

object ClientCommunication extends Controller {

    var percentages = Map[String, Int]()

    var messages = Map[String, Html]()

    var clearContent = Map[String, Boolean]()

    def getPercentage(name: String) = Action {
        implicit request =>
            val value = if (percentages.contains(name)) percentages(name) else 0
            Ok(value.toString).as(HTML)
    }

    def getMessage(name: String) = Action {

        implicit request =>
            if (clearContent.getOrElse(name, false)) {
                clearContent(name) = false
                Ok("clear")
            } else {
                val content = messages.getOrElse(name, Html(""))
                messages.remove(name)
                Ok(content)
            }
    }

    def increasePercentage(name: String, value: Int) = {
        percentages(name) = (percentages.getOrElseUpdate(name, 0)) + value
    }

    def setPercentage(name: String, value: Int) = {
        percentages(name) = value
    }

    def addMessage(name: String, message: AdminMessage) = {
        addHtml(name, views.html.admin.snippets.message(message))
    }

    def clear(name: String) = {
        clearContent(name) = true
    }

    def addHtml(name: String, html: Html) = {
        messages(name) = html + messages.getOrElseUpdate(name, Html(""))
    }
} 
