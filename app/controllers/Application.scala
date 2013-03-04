package controllers

import play.api.mvc._
import models._

object Application extends Controller {

    // Global name for the application
    val name = "ColouR"


    def indexDefault(any : String) = Action {
        implicit request =>
            Ok(views.html.index(Conference.count()))
    }

    def index() = Action {
        implicit request =>
            Ok(views.html.index(Conference.count()))
    }

    def test = Action {
        implicit request =>
            val confList = List((1, 2), (2, 4), (3, 3))

            // DetectorFactory.loadProfile("cache/profiles/")
            // val detector = DetectorFactory.create()
            // detector.append("Kanagawa-Ken Seishin Igakkaishi")

            /*
            models.Field.all(OrderBy("name") :: Limit(4)).foreach { field =>
                println(field)
                field.subFields.foreach { subfield =>
                }
            }
            detector.getProbabilities().foreach { p =>
                println(p.lang + ": " + p.prob)
            }
             */
            // Ok(detector.detect())
            Ok("")
    }
}

