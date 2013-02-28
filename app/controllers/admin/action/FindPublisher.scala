package controllers.admin.action

import play.api.mvc._
import models._
import controllers.admin._


object FindPublisher extends AdminAction {
    val description = "Update the publisher depending on the name and short name. "

    val label = "Find publisher"

    val name = "publisher"

    override def run(request: Request[AnyContent]) = {
        val publishers = Publisher.all()
        val increment = (100 / publishers.size)
        ClientCommunication.setPercentage("publisher", 0)

        publishers.foreach {
            publisher =>
                if (publisher.name != "") {
                    Conference.updatePublisher("%" + publisher.name + "%", publisher.id)
                    increasePercentage(increment)
                    infoMessage("Done updating publisher " + publisher.name)
                }
        }
        successMessage("Finished :)")
        setPercentage(100)
    }
}
