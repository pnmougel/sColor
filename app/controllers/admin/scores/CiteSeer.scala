package controllers.admin.scores

import play.api.mvc._
import models._
import controllers.admin.action._
import controllers.utils.HtmlCache
import controllers.utils.Utils
import collection.JavaConversions._

object CiteSeer extends AdminAction {
    val description = "This action gather the rankings from CiteSeer."

    val name = "citeseer"

    val label = "CiteSeer"

    override val category = "External Ranking"
    override val icon = "star-empty"

    def setScore(conferenceId: Long, score: Int) = {
        /*
        if(!ExternalScore.getRankingForConference(conferenceId, rankingId).isDefined) {
            ExternalScore.createOrUpdate(conferenceId, rankingId, score, "toto", 45)
        }
        */
    }

    var rankingId: Long = 0

    override def run(request: Request[AnyContent]) = {
        rankingId = ExternalRanking.getOrCreate("http://citeseerx.ist.psu.edu/stats/venues",
            "CiteSeer",
            "Description CiteSeer",
            ScoreType.getOrCreate("hIndex citeseer", "hIndex citeseer"))
        ExternalScore.deleteByExternalRanking(rankingId)
        // var matches = new ListBuffer[ConferenceMatch]

        clearMessages()

        var nbMatchingOne = 0
        var nbNotMatching = 0

        val docOpt = HtmlCache.getDocument("citeseer", "citeseer", "http://citeseerx.ist.psu.edu/stats/venues")
        if (docOpt.isDefined) {
            val doc = docOpt.get
            val rows = doc.select("ol")
            rows.foreach {
                row =>
                    val lis = row.select("li")
                    lis.foreach {
                        li =>
                            val confDblpUrl = li.select("a").attr("href")
                            val confName = li.select("a").text.replace("/", "_")
                            val dblpPageOpt = HtmlCache.getDocument("citeseer/dblp/", confName, confDblpUrl)
                            if (dblpPageOpt.isDefined) {
                                val (nameFull, pContent) = Utils.getParenthesisContent(dblpPageOpt.get.select("h1").text)
                                val shortName = if (pContent.size == 1) {
                                    pContent(0)
                                } else {
                                    ""
                                }

                                // val confName = li.select("a").text
                                /*
                                val matchingConference = Utils.findMatchingConference(nameFull.toLowerCase(), shortName.toLowerCase())
                              if(matchingConference.isDefined) {
                                  nbMatchingOne += 1
                              } else {
                                  nbNotMatching += 1
                              }
                              */
                                // val score = li.select("em").text
                            }
                    }
            }
        }
        println("Nb matching one " + nbMatchingOne)
        println("Nb matching none " + nbNotMatching)
    }
}