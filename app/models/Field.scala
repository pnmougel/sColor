package models

import anorm._
import models.renorm.OrderBy
import models.renorm.Table

case class Field(var name: String) {
    var id: Long = 0

    implicit val instance = this
    lazy val subfields = Field.oneToMany[SubField](SubField)
}

object Field extends Table[Field] {
    def getByName(name: String) = findOption('name -> name)

    def all() = super.all(OrderBy("name"))

    def getOrCreate(name: String): Long = super.getOrCreate('name -> name)
}