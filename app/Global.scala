import play.api.Play.current
import play.api.Application
import play.api.GlobalSettings
import play.api.mvc.Handler
import play.api.mvc.RequestHeader
import play.api.db.DB

import scala.slick.driver.PostgresDriver.simple._
import Database.threadLocalSession

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
        lazy val database = Database.forDataSource(DB.getDataSource())
    }

    override def onRouteRequest(request: RequestHeader): Option[Handler] = {
        /*
        org.squeryl.Session.currentSessionOption.foreach(_.unbindFromCurrentThread)
        */
        super.onRouteRequest(request)
    }
}