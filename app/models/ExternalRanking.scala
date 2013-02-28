package models

import anorm._
import models.renorm.Table

case class ExternalRanking(url: String, name: String, description: String, scoretypeId: Long) {
    var id: Long = 0

    lazy val scoreType = ScoreType.byId(scoretypeId).get
}

object ExternalRanking extends Table[ExternalRanking] {

    lazy val microsoftRankingId = ExternalRanking.getOrCreate("http://academic.research.microsoft.com/", "Microsoft AR",
        "Ranking based on the h-index computed using the citations found by the Microsoft web search engine Bing.",
        ScoreType.getOrCreate("h-index", "h-index"))
    lazy val coreJournalRankingId = ExternalRanking.getOrCreate("http://core.edu.au/", "Core",
        "Journal ranking made by the Computing Research and Education Association of Australasia, an association of university departments of computer science in Australia and New Zealand.",
        ScoreType.getOrCreate("A*ABC", "A*ABC"))
    lazy val coreConferenceRankingId = ExternalRanking.getOrCreate("http://core.edu.au/", "Core",
        "Conference ranking made by the Computing Research and Education Association of Australasia, an association of university departments of computer science in Australia and New Zealand.",
        ScoreType.getOrCreate("ABC", "ABC"))


    def getOrCreate(url: String, name: String, description: String, scoreTypeId: Long) =
        super.getOrCreate('url -> url, 'name -> name, 'description -> description, 'scoretype_id -> scoreTypeId)
}