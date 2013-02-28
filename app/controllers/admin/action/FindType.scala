package controllers.admin.action

import controllers.admin.form.AdminForm
import models._
import play.api.mvc._
import controllers.admin.form.SelectField

object FindType extends AdminAction {
    val description = "This action update the type (i.e., conference, journal or workshop) depending on the name."

    val name = "type"

    val label = "Find types"

    override val form = AdminForm(SelectField("a", "b", List("bla" -> "")))

    override def run(request: Request[AnyContent]) = {

        val conferenceTypeId = CType.getOrCreate("Conference")
        Conference.updateType("%conference%", conferenceTypeId)
        Conference.updateType("%symposium%", conferenceTypeId)
        Conference.updateType("% conf %", conferenceTypeId)

        infoMessage("Done updating Conference")
        setPercentage(33)

        val workshopTypeId = CType.getOrCreate("Workshop")
        Conference.updateType("%workshop%", workshopTypeId)
        infoMessage("Done updating journals")
        setPercentage(66)

        val journalTypeId = CType.getOrCreate("Journal")
        Conference.updateType("%journal%", journalTypeId)
        infoMessage("Done updating workshops")

        setPercentage(100)
        successMessage("Finished :)")
    }
}