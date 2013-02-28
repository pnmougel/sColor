package controllers.utils.stemmer

import scala.collection.mutable.HashSet

object Stemmer {

    val stemmer = new EnglishStemmer()

    def getStems(name: String): HashSet[String] = {
        var stems = new HashSet[String]()
        controllers.utils.Utils.cleanName(name).split(" ").map {
            w =>
                stems.add(stem(w))
        }
        stems
    }

    def stem(word: String): String = {
        stemmer.setCurrent(word)
        stemmer.stem()
        stemmer.getCurrent()
    }

}