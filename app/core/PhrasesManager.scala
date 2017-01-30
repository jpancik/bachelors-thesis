package core

import javax.inject.Inject

import java.io.File
import java.nio.file.{Paths, Files}
import javax.inject.Singleton

import models.{Phrase, PhrasesDAO}
import org.apache.commons.lang3.StringEscapeUtils

import scala.collection.mutable.{ArrayBuffer, HashMap}
import scala.concurrent.{ExecutionContext, Await, Future}
import scala.concurrent.duration._
import ExecutionContext.Implicits.global

@Singleton
class PhrasesManager @Inject()(val phrasesDao: PhrasesDAO) {
    def getSuggestions(input: String): Future[List[Suggestion]] = {
        val splitted = input.replaceAll("[,.!?]", "").toLowerCase.split(' ');

        val futures = new ArrayBuffer[Future[Seq[Suggestion]]]
        for (i <- 0 until splitted.length) {
            for (j <- i until splitted.length) {
                // Get selector.
                var string = ""
                for(h <- i to j) {
                    string = string + splitted(h)
                    if (h < j) {
                        string += " "
                    }
                }
                val selector = string

                // Try the selector against db.
                val begin = i
                val end = j
                val futureSuggestions = for (phrases <- phrasesDao.retrieveByOriginal(selector)) yield {
                    for (phrase <- phrases) yield {
                        new Suggestion(StringEscapeUtils.unescapeHtml4(phrase.translation),
                            StringEscapeUtils.unescapeHtml4(phrase.original),
                            begin,
                            end,
                            phrase.directProbability.toFloat,
                            phrase.lexicalWeighting.toFloat)
                    }
                }
                futureSuggestions recover {
                    case timeout: java.util.concurrent.TimeoutException => {
                        System.err.println("Timeout on phrase " + selector + "!")
                        timeout.printStackTrace(System.err)
                    }
                }
                futures += futureSuggestions
            }
        }

        for(suggestions <- Future.sequence(futures.toList)) yield {
            val out = new ArrayBuffer[Suggestion]
            for(sequence <- suggestions) {
                for(item <- sequence) {
                    out += item
                }
            }
            out.toList
        }
    }
}
