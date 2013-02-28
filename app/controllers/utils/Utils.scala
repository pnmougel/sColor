package controllers.utils

import scala.Array.canBuildFrom
import scala.collection.mutable.Map
import scala.collection.mutable.Set

object Utils {
    /*
    Remove articles and several words like symposium, conference, journal.
    Remove also parenthesis and what they contains, all non alphabetical characters, inclucluding ponctuation and numbers
    */
    val replacementWords = Map(
        "int" -> "international",
        "conf" -> "conference",
        "trans" -> "transaction",
        "sci" -> "science",
        "jour" -> "journal")
    val shortWords = Set("on", "at", "the", "a", "in", "to", "of", "for", "and", "into")
    val commonWords = Set("international", "conference", "workshop", "journal", "symposium", "transaction", "int.", "conf.", "trans.", "jour.", "ieee", "acm")

    def cleanName(name: String): String = {
        var lowerName = name.toLowerCase()

        // var (curName, p) = getParenthesisContent(lowerName)
        // curName += " " + p.mkString(" ")

        // remove plural forms
        // var words = curName.split("[/ -]").map(word => if (word.endsWith("s")) { word.substring(0, word.size - 1) } else { word })
        var words = lowerName.split("[/ -]")

        // Remove non alphabetical characters
        words = words.map(word => word.filter(c => c.isLetter || c.isWhitespace))

        // Remove short words
        words = words.filter(word => !shortWords.contains(word) && !word.isEmpty)

        // Remove common words
        // words = words.filter(word => !commonWords.contains(word) && !word.isEmpty)

        // Replace some short words with full word
        words.map(word => replacementWords.getOrElse(word, word)).mkString(" ")
    }

    // Remove the texts between parenthesis and return them as a list
    def getParenthesisContent(data: String): (String, List[String]) = {
        var curData = data
        var parenthesisContents = List[String]()
        var parenthesisStart = 0
        var parenthesisEnd = 0
        do {
            parenthesisStart = curData.indexOf("(")
            parenthesisEnd = curData.indexOf(")", parenthesisStart)
            if (parenthesisStart >= 0 && parenthesisEnd > 0) {
                val parenthesisContent = curData.substring(parenthesisStart + 1, parenthesisEnd)
                parenthesisContents = parenthesisContent :: parenthesisContents
                val toRemove = if (parenthesisStart != 0 && parenthesisEnd != curData.size - 1 && curData(parenthesisStart - 1) == ' ' && curData(parenthesisEnd + 1) == ' ') {
                    "(" + parenthesisContent + ") "
                } else {
                    "(" + parenthesisContent + ")"
                }
                curData = curData.replace(toRemove, "")
            }
        } while (parenthesisStart >= 0 && parenthesisEnd > 0)

        (curData.trim(), parenthesisContents)
    }


    /*
    def findMatchingConference(name : String, shortName : String = "", fieldId : Option[Long] = None) : Option[Conference] = {
        // First try on name
        var confTmp = Conference.findByName(name, fieldId)
        var matchingConference = getBestMatch(name, confTmp)
        if(!matchingConference.isDefined) {
            // Try on short name
            if(!shortName.trim.isEmpty()) {
		        var confTmp = Conference.findByShortName(shortName, fieldId)
            	matchingConference = getBestMatch(name, confTmp)
            }
            if(!matchingConference.isDefined) {
                // Try on matching name
	            matchingConference = getBestMatch(name, Conference.findByMatchingName(cleanName(name), fieldId))
	        }
        }
        matchingConference
    }
    */

    /*
    def getBestMatch(name : String, conferences : List[Conference]) : Option[Conference] = {
        if(conferences.size == 1) {
            Option(conferences(0))
        } else if(conferences.size > 1 && conferences.size < 100) {
            val minConference = conferences.minBy { conference => stringDistance(conference.name, name) }
            if(stringDistance(minConference.name, name) < 0.3333) {
                Option(minConference)
            } else {
                None
            }
        } else {
            None
        }
    }
    */

    // Compute Levensthein distance between two string
    // The lower is the value, the more similar are the strings
    /*
    def stringDistance(s1: String, s2: String): Double = {
        val memo = scala.collection.mutable.Map[(List[Char], List[Char]), Int]()
            def min(a: Int, b: Int, c: Int) = math.min(math.min(a, b), c)
            def sd(s1: List[Char], s2: List[Char]): Int = {
                if (!memo.contains((s1, s2)))
                    memo((s1, s2)) = (s1, s2) match {
                        case (_, Nil) => s1.length
                        case (Nil, _) => s2.length
                        case (c1 :: t1, c2 :: t2) => min(sd(t1, s2) + 1, sd(s1, t2) + 1,
                            sd(t1, t2) + (if (c1 == c2) 0 else 1))
                    }
                memo((s1, s2))
            }

        sd(s1.toList, s2.toList).toDouble / (s2.size)
    }
    */

}