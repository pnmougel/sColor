import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "Colour"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
        javaJdbc, javaEbean, anorm,
        // Add your project dependencies here
        // "org.scalatest" %% "scalatest" % "1.8" % "test",
        // "org.squeryl" %% "squeryl" % "0.9.5-2",
        "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
        "mysql" % "mysql-connector-java" % "5.1.18",
        "com.jquery" % "jquery" % "1.7.1"
        // "com.typesafe" % "slick_2.10.0-M4" % "0.10.0-M2"
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(
        // Add your own project settings here
        resolvers += "webjars" at "http://webjars.github.com/m2"
    )
}
