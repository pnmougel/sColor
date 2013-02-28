package controllers

import play.api.mvc._

object StaticPage extends Controller {
    def widget() = Action {
        implicit request =>
            Ok(views.html.staticpages.widget(request))
    }
}