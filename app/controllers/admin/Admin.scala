package controllers.admin

import play.api.mvc._
import controllers.admin.action._
import controllers.admin.scores._
import scala.collection.mutable._
import play.api.Play.current
import play.api.libs.concurrent.Akka

object Admin extends Controller {

    var actionsByCategory = new HashMap[String, ListBuffer[AdminAction]]
    var actionsMap = new HashMap[String, AdminAction]()

    registerAction(FindType)
    registerAction(FindPublisher)
    registerAction(MicrosoftAR)
    registerAction(Clear)
    registerAction(Core)
    registerAction(CiteSeer)
    registerAction(TestAction)

    def index = Action {
        implicit request =>
            Ok(views.html.admin.index())
    }

    def registerAction(action: AdminAction) = {
        val actions = actionsByCategory.getOrElseUpdate(action.category, new ListBuffer())
        actions += action
        actionsMap(action.name) = action
    }

    def selectAction(actionName: String) = Action {
        implicit request =>
            if (actionsMap.contains(actionName)) {
                Ok(views.html.admin.action(actionsMap(actionName)))
            } else {
                Ok(views.html.admin.index())
            }

    }

    def run(actionName: String) = Action {
        implicit request =>
            if (actionsMap.contains(actionName)) {
                val promiseOfResult = Akka.future {
                    actionsMap(actionName).run(request)
                }
                Async {
                    promiseOfResult.map {
                        i => Ok("")
                    }
                }
            } else {
                // It should be an error message
                Ok(views.html.admin.index())
            }
    }

    /*
    def list = Action {
        var methodNames = ""
        Conference.getClass.getMethods foreach { method =>
            methodNames += method + " - " + method.getParameterTypes.size + "\n"
            method.getParameterTypes.foreach { p =>
                methodNames += "\t" + p.getName + "\n"
            }
            method.getParameterAnnotations.foreach { m =>
                m.foreach { x =>
                    methodNames += "\t" + x.toString + "\n"
                }
            }
        }
        Ok(methodNames)
        
    }
    */
}