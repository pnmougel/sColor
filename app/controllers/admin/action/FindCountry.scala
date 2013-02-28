package controllers.admin.action

import play.api.mvc._
import models._


object FindCountry extends AdminAction {
    val description = "Try to guess the conference / journal region from the language used."

    val label = "Find Region"

    val name = "region"

    override def run(request: Request[AnyContent]) = {
        val publishers = Publisher.all()
        val increment = (100 / publishers.size)

        setPercentage(0)

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
