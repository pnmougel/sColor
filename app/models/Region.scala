package models

import anorm._
import play.api.db._
import play.api.Play.current
import models.renorm.Where
import models.renorm.OrderBy
import models.renorm.Table

case class Region(name: String, adjective: Option[String], isInternational: Boolean) {
    var id: Long = 0

    def adjectiveT = if (adjective.isDefined) adjective.get else name
}

object Region extends Table[Region] {
    def getInternationalRegions() = select(Where(Seq('is_international -> true)) :: OrderBy("id"))

    def getCountries() = select(Where(Seq('is_international -> false)) :: OrderBy("name"))

    lazy val getFrequentCountries: List[Region] = DB.withConnection {
        implicit c =>
        /*
        SQL("""SELECT r.*
            FROM Region r, Conference c
            WHERE c.region_id = r.id
            AND r.is_international = false
            GROUP BY r.id
            ORDER BY count(*) DESC
            LIMIT 10;""").as(single *)
            */
            SQL( """SELECT r.*
            FROM Region r
            WHERE r.is_international = false
            LIMIT 10;""").as(single *)
    }

    def create(name: String, adjectiveOpt: Option[String], isInternational: Boolean): Long = {
        createEntry('name -> name, 'adjective -> adjectiveOpt, 'is_international -> isInternational)
    }

    def getOrCreate(name: String, adjectiveOpt: Option[String], isInternational: Boolean): Long = {
        var region = findOption('name -> name)
        if (!region.isDefined) {
            createEntry('name -> name, 'adjective -> adjectiveOpt, 'is_international -> isInternational)
        } else {
            region.get.id
        }
    }
}