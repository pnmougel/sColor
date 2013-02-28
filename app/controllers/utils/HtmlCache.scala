package controllers.utils

import java.io.PrintWriter
import java.io.File
import scala.io.Source
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

object HtmlCache {
    def getDocument(directory: String, fileName: String, url: String): Option[Document] = {
        val dirFile = new File("cache/" + directory)
        dirFile.mkdirs()
        val cacheFile = new File("cache/" + directory + "/" + fileName)
        if (cacheFile.exists()) {
            val html = Source.fromFile(cacheFile).getLines().mkString("\n")
            Option(Jsoup.parse(html))
        } else {
            if (Jsoup.connect(url).timeout(200000).ignoreHttpErrors(true).execute().statusCode() != 200) {
                return None
            }
            val tmpDoc = Jsoup.connect(url).timeout(200000).get()
            val htmlFile = new PrintWriter(cacheFile)
            htmlFile.write(tmpDoc.body().toString())
            htmlFile.flush()
            htmlFile.close()
            Option(tmpDoc)
        }
    }
}