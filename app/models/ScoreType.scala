package models

import anorm._
import models.renorm.Table

case class ScoreType(name: String, description: String) {
    var id: Long = 0
}

object ScoreType extends Table[ScoreType](Option("scoretype")) {
    def getOrCreate(name: String, description: String) = super.getOrCreate('name -> name, 'description -> description)
}