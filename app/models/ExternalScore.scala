package models

import anorm._
import models.renorm.Table
import play.api.Play.current
import play.api.db._
import models.renorm.Where

case class ExternalScore(conferenceId: Long, externalrankingId: Long, score: Double, scoreText: String) {
    lazy val externalRanking = ExternalRanking.byId(externalrankingId).get
}

object ExternalScore extends Table[ExternalScore](Option("conference_externalranking")) {
    override val idColumn = ""

    def getByConferenceId(conferenceId: Long) = find('conference_id -> conferenceId)

    def getRankingForConference(conferenceId: Long, externalRankingId: Long) =
        findOption('conference_id -> conferenceId, 'externalranking_id -> externalRankingId)

    def deleteByExternalRanking(externalRankingId: Long) = deleteWhere('externalranking_id -> externalRankingId)

    def deleteByConference(conferenceId: Long) = {
        deleteWhere('conference_id -> conferenceId)
        Conference.updateExternalScoreForConference(conferenceId)
    }

    def createOrUpdate(
                        conferenceId: Long,
                        externalrankingId: Long,
                        score: Double,
                        scoreText: String) = DB.withConnection {
        implicit c =>

        // If the score already exist, it is updated
            val externalScore = findOption('conference_id -> conferenceId, 'externalranking_id -> externalrankingId)
            if (externalScore.isDefined) {
                if (score > externalScore.get.score) {
                    updateWhere(Where(Seq('conference_id -> conferenceId, 'externalranking_id -> externalrankingId)), 'score -> score, 'score_text -> scoreText)
                }
            } else {
                createEntry(
                    'conference_id -> conferenceId,
                    'externalranking_id -> externalrankingId,
                    'score -> score,
                    'score_text -> scoreText)
            }
            Conference.updateExternalScoreForConference(conferenceId)
    }
}