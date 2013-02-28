package controllers.admin.action

import play.api.mvc._
import models._
import controllers.admin.scores.ConferenceInformation


object TestAction extends AdminAction {
    val description = "Action for testing purpose"

    val label = "Test"

    val name = "test"

    case class TestConferenceInformation(override val name: String, override val shortName: Option[String],
                                         override val ctype: Option[CType] = None,
                                         override val field: Option[models.Field] = None) extends ConferenceInformation(name, shortName, ctype, field)

    override def run(request: Request[AnyContent]) = {
        infoMessage("Bla bla")

        addHtml(views.html.admin.snippets.mergeConference(1,
            TestConferenceInformation("SIGK", Option("Bla"), CType.byId(CType.conferenceType), None),
            List((Conference.byId(19).get, 1.5), (Conference.byId(20).get, 1.5), (Conference.byId(21).get, 1.5))))

        infoMessage("Something else")
    }
}
