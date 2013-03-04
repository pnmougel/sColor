import models.Conference
import play.api.Application
import play.api.GlobalSettings
import play.api._
import play.api.mvc._
import play.api.mvc.Results._

object Global extends GlobalSettings {
    override def onStart(app: Application) {

        // Tasks that will have to be done on a regular basis
        // Build a dump of the database
        // If we build a dictionary for the stems, update the dictionary
        // Remove reseted password query
        /*
        Akka.system.scheduler.schedule(Duration.create(0, TimeUnit.MILLISECONDS), Duration.create(30, TimeUnit.DAYS)) {
        }
        */

        // models.Stem.buildDictionary()

        // DetectorFactory.loadProfile("cache/profiles/")

        /*
        SessionFactory.externalTransactionManagementAdapter = Some(() => {
            Option({
                val session : Session = new Session(DB.getConnection()(app), new PostgreSqlAdapter(), None) {
                    override def cleanup = {
                        super.cleanup
                        unbindFromCurrentThread
                    }
                }
                session.bindToCurrentThread
                session
            })
        })
        SessionFactory.concreteFactory = Some(() => getSession(new PostgreSqlAdapter, app))
        */
    }


    /**
     * Redirect to main page
     * @param r
     * @return
     */
    override def onHandlerNotFound(r : RequestHeader) = {
        Ok(views.html.index(Conference.count())(r))
    }

    override def onRouteRequest(request: RequestHeader): Option[Handler] = {
        /*
        org.squeryl.Session.currentSessionOption.foreach(_.unbindFromCurrentThread)
        */
        super.onRouteRequest(request)
    }
}