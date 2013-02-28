package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import java.util.Date
import models.renorm.Table

case class UserVote(conferenceId: Long, userId: Long, score: Double, date: Date)

case class UserVotes(avgUserScore: Double, totalVotes: Int, voteA: (Int, Int), voteB: (Int, Int), voteC: (Int, Int), voteD: (Int, Int))


object UserVote extends Table[UserVote](Option("conference_scoreuser")) {
    val voteA = 5D
    val voteB = 4D
    val voteC = 3D
    val voteD = 1D

    override val single = {
        get[Long]("conference_id") ~ get[Long]("user_id") ~ get[Double]("score") ~ get[Date]("created_at") map {
            case conferenceId ~ userId ~ score ~ date => UserVote(conferenceId, userId, score, date)
        }
    }

    def getUserVotesByConferenceId(conferenceId: Long): UserVotes = {
        var nbVoteA = 0
        var nbVoteB = 0
        var nbVoteC = 0
        var nbVoteD = 0
        getByConferenceId(conferenceId).foreach {
            vote =>
                if (vote.score == voteA) {
                    nbVoteA += 1
                }
                if (vote.score == voteB) {
                    nbVoteB += 1
                }
                if (vote.score == voteC) {
                    nbVoteC += 1
                }
                if (vote.score == voteD) {
                    nbVoteD += 1
                }
        }
        val totalVotes: Int = nbVoteA + nbVoteB + nbVoteC + nbVoteD
        val percVoteA = 1.0 * nbVoteA / totalVotes * 100
        val percVoteB = 1.0 * nbVoteB / totalVotes * 100
        val percVoteC = 1.0 * nbVoteC / totalVotes * 100
        val percVoteD = 1.0 * nbVoteD / totalVotes * 100
        val avgUserScore = if (totalVotes == 0) {
            -1
        } else {
            scala.math.max(0, (-1 * nbVoteD + nbVoteC + 2 * nbVoteB + 3 * nbVoteA) * 5.0 / (3 * totalVotes))
        }
        UserVotes(avgUserScore, totalVotes, (nbVoteA, percVoteA.toInt), (nbVoteB, percVoteB.toInt), (nbVoteC, percVoteC.toInt), (nbVoteD, percVoteD.toInt))
    }

    def userVoteForConference(conferenceId: Long, userId: Option[User]) = {
        if (userId.isDefined) {
            findOption('conference_id -> conferenceId, 'user_id -> userId.get.id)
        } else {
            None
        }
    }

    def getByConferenceId(conferenceId: Long): List[UserVote] = find('conference_id -> conferenceId)

    def create(conferenceId: Long, userId: Long, score: Double, date: Date) {
        createEntry('conference_id -> conferenceId, 'user_id -> userId, 'score -> score, 'created_at -> date)
        Conference.updateUserScoreForConference(conferenceId)
    }

    def delete(conferenceId: Long, userId: Long) {
        deleteWhere('conference_id -> conferenceId,
            'user_id -> userId)
        Conference.updateUserScoreForConference(conferenceId)
    }

    def deleteByConference(conferenceId: Long) = DB.withConnection {
        implicit c =>
            deleteWhere('conference_id -> conferenceId)
            Conference.updateUserScoreForConference(conferenceId)
    }

}