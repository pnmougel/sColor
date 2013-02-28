package models

import anorm._
import models.renorm.Table

case class Publisher(name: String) {
    var id: Long = 0
}

object Publisher extends Table[Publisher] {
    def getByName(name: String) = findOption('name -> name)

    def create(name: String) = createEntry('name -> name)

    def getOrCreate(name: String) = super.getOrCreate('name -> name)
}