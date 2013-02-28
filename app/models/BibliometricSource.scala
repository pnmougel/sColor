package models

import anorm._
import models.renorm.Table

case class BibliometricSource(name: String, url: String) {
    var id: Long = 0
}

object BibliometricSource extends Table[BibliometricSource] {
    lazy val dblpSource = getOrCreate("Dblp", "http://dblp.org/")
    lazy val microsoftARSource = getOrCreate("Microsoft AR", "http://academic.research.microsoft.com/")
    lazy val googleScholarSource = getOrCreate("Google Scholar", "http://scholar.google.com/")

    def getOrCreate(name: String, url: String): Long = super.getOrCreate('name -> name, 'url -> url)
}