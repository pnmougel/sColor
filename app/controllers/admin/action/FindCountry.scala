package controllers.admin.action

import play.api.mvc._
import models._


object FindCountry extends AdminAction {
    val description = "Try to guess the conference / journal region from its name. It could be possible to detect it using a language detection library."

    val label = "Find Region"

    val name = "region"

    override def run(request: Request[AnyContent]) = {
        val regionsWithAdjectives = Region.regionsWithAdjectives()
        val increment = (100 / regionsWithAdjectives.size)

        setPercentage(0)
        regionsWithAdjectives.foreach { region =>
            Conference.updateRegion("%" + region.adjectiveT + "%", region.id)
            Conference.updateRegion("%" + region.name + "%", region.id)
            increasePercentage(increment)
        }
        successMessage("Done updating the regions")
        setPercentage(100)
    }
}
