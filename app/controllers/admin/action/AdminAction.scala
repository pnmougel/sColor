package controllers.admin.action

import play.api.mvc._
import controllers.admin._
import controllers.admin.AdminMessage
import controllers.admin.form.AdminForm
import play.api.templates.Html

abstract class AdminAction extends Controller {
    val description: String

    val name: String

    val label: String

    val category = "Actions"

    val icon = "cog"

    def form() = new AdminForm()

    def run(request: Request[AnyContent])


    def setPercentage(value: Int) = {
        ClientCommunication.setPercentage(name, value)
    }

    def increasePercentage(value: Int) = {
        ClientCommunication.increasePercentage(name, value)
    }

    /* Send text messages to the client
    */

    def infoMessage(message: String) = {
        ClientCommunication.addMessage(name, new AdminMessage(message, "info", "Info"))
    }


    def errorMessage(message: String) = {
        ClientCommunication.addMessage(name, new AdminMessage(message, "important", "Error"))
    }

    def warningMessage(message: String) = {
        ClientCommunication.addMessage(name, new AdminMessage(message, "warning", "Warning"))
    }


    def successMessage(message: String) = {
        ClientCommunication.addMessage(name, new AdminMessage(message, "success", "Success"))
    }

    def clearMessages() = {
        ClientCommunication.clear(name)
    }

    def addHtml(html: Html) = {
        ClientCommunication.addHtml(name, html)
    }
}