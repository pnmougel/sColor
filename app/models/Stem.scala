package models

import anorm._
import play.api.Play.current
import play.api.db._
import models.renorm.Table
import java.io.FileInputStream
import java.io.FileOutputStream
import scala.util.Marshal
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import java.io.File
import controllers.utils.stemmer.Stemmer

case class Stem(stem: String) {
    var id: Long = 0

    def conferences: List[Conference] = DB.withConnection {
        implicit c =>
            SQL("SELECT conference.* FROM conference, conference_stem " +
              "WHERE conference_stem.stem_id = {id} AND conference_stem.conference_id = conference.id").on(
                'id -> id).as(Conference.single *)
    }
}

object Stem extends Table[Stem] {

    var stemToPublications = HashMap[String, (List[Long], Int)]()
    var publicationToStems = HashMap[Long, List[String]]()

    def getOrCreate(stem: String): Stem = {
        val stemId = super.getOrCreate('stem -> stem)
        var stemEntry = Stem(stem)
        stemEntry.id = stemId
        stemEntry
    }

    def addStemForConference(name: String, publicationId: Long) = DB.withConnection {
        implicit c =>
            Stemmer.getStems(name).foreach {
                stem =>
                    val stemEntry = getOrCreate(stem)
                    SQL("INSERT INTO conference_stem VALUES ({stemId}, {publicationId})").on(
                        'publicationId -> publicationId,
                        'stemId -> stemEntry.id).executeInsert()
                    stemToPublications(stem) = {
                        val (conferences, nbStems) = stemToPublications.getOrElse(stem, (List(), 0))
                        (publicationId :: conferences, nbStems + 1)
                    }
                    publicationToStems(publicationId) = stem :: publicationToStems.getOrElse(publicationId, List())
            }
    }

    val minScoreToMatch = 0.8D

    def buildDictionary(useCache: Boolean = true) = {
        val stemToPublicationsFile = new File("cache/stemToPublications")
        val publicationToStemsFile = new File("cache/publicationToStems")
        if (stemToPublicationsFile.exists() && publicationToStemsFile.exists() && useCache) {
            val in = new FileInputStream(stemToPublicationsFile)
            val bytes = Stream.continually(in.read).takeWhile(-1 !=).map(_.toByte).toArray
            stemToPublications = Marshal.load[HashMap[String, (List[Long], Int)]](bytes)
            in.close()
            val in2 = new FileInputStream(publicationToStemsFile)
            val bytes2 = Stream.continually(in2.read).takeWhile(-1 !=).map(_.toByte).toArray
            publicationToStems = Marshal.load[HashMap[Long, List[String]]](bytes2)
            in2.close()
        } else {
            models.Stem.all().foreach {
                stem =>
                    stemToPublications(stem.stem) = (stem.conferences.map {
                        publication =>
                            publicationToStems(publication.id) = stem.stem :: publicationToStems.getOrElse(publication.id, List[String]())
                            publication.id
                    }, stem.conferences.size)
            }
            val serializedDict = new FileOutputStream(stemToPublicationsFile)
            serializedDict.write(Marshal.dump(stemToPublications))
            serializedDict.flush()
            serializedDict.close()
            val serializedDict2 = new FileOutputStream(publicationToStemsFile)
            serializedDict2.write(Marshal.dump(publicationToStems))
            serializedDict2.flush()
            serializedDict2.close()
        }
    }

    def getMatchingConferences(stems: HashSet[String]): List[(Long, Double)] = {
        var matchingPublications = new HashMap[Long, Double]()
        var maxScore = 0D

        stems.foreach {
            word =>
                val stem = Stemmer.stem(word)
                val (matchingOnStem, weight) = stemToPublications.getOrElse(stem, (List(), 0))
                val curWeight: Double = (1.0D / weight)
                maxScore += curWeight
                matchingOnStem.foreach {
                    publication =>
                        matchingPublications(publication) = matchingPublications.getOrElse(publication, 0D) + curWeight
                }
        }
        matchingPublications = matchingPublications.filter(_._2 > minScoreToMatch).map {
            case (publication, score) =>
                var localMaxScore = 0D
                var curScore = 0D
                publicationToStems(publication).foreach {
                    stem =>
                        val (matchingOnStem, weight) = stemToPublications.getOrElse(stem, (List(), 0))
                        val curWeight: Double = (1.0D / weight)
                        localMaxScore += curWeight
                        if (stems.contains(stem)) {
                            curScore += curWeight
                        }
                }
                val finalScore = math.min(curScore / localMaxScore, score / maxScore)
                (publication, finalScore)
        }
        matchingPublications.filter(_._2 > minScoreToMatch).toList.sortWith((a, b) => a._2 > b._2)
    }
}