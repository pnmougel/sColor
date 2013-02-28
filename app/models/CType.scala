package models

import anorm._
import models.renorm.Table

case class CType(name: String) {
    var id: Long = 0
}

object CType extends Table[CType] {
    lazy val conferenceType = getOrCreate("Conference")
    lazy val journalType = getOrCreate("Journal")
    lazy val workshopType = getOrCreate("Workshop")

    def create(name: String): Long = createEntry('name -> name)

    def getOrCreate(name: String): Long = super.getOrCreate('name -> name)
}