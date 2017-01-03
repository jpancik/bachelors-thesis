package core

import java.io.File
import java.nio.file.{Paths, Files}
import javax.inject.Singleton

import org.apache.commons.lang3.StringEscapeUtils

import scala.collection.mutable.{ArrayBuffer, HashMap}
import scala.io.Source

@Singleton
class PhrasesManager {
    println("Phrases manager is being initialized!")
    val phrases : HashMap[String, ArrayBuffer[Phrase]] = new HashMap[String, ArrayBuffer[Phrase]]

    //val file = "en-cs.phrase-table.1000.txt"
    val file = "en-cs.phrase-table.500000.txt"
    println("Loading file: " + file)
    try {
        val matchingRegex = """^([^\t|]+) \|\|\| ([^\t|]+) \|\|\| [^ ]+ [^ ]+ ([^ ]+) ([^ ]+) .*$""".r
        var lines = 0
        for (line <- Source.fromFile("data/" + file).getLines()) {
            lines += 1
            if(lines % 1000000 == 0) {
                println("Processed " + (lines/1000) + "k lines.")
            }

            val result = matchingRegex.findFirstMatchIn(line)
            if (result.isDefined && lines % 2 == 0) {
                val original = StringEscapeUtils.unescapeHtml4(result.get.group(1))
                val translation = StringEscapeUtils.unescapeHtml4(result.get.group(2))
                val directProbability = result.get.group(3).toFloat
                val lexicalWeighting = result.get.group(4).toFloat
                val phrase = new Phrase(original, translation, directProbability, lexicalWeighting)
                if (phrases.contains(original)) {
                    phrases get(original) match {
                        case None =>
                        case Some(list) =>
                            list += phrase
                    }
                } else {
                    val list = new ArrayBuffer[Phrase]
                    list += phrase
                    phrases.put(original, list)
                }
            }
        }
    } catch {
        case e: Exception => {
            println("Failed loading file.")
            e.printStackTrace()
        }
    }
    println("Finished loading phrases.")
    //getSuggestions("i have found a new car on the street in front of the house")

    class TreeNode(val phrases: ArrayBuffer[Suggestion],
                        val translated: ArrayBuffer[Boolean],
                        val children: ArrayBuffer[TreeNode]) {

        def getProbability: Float = {
            var out = 1.0f
            for (phrase <- phrases) {
                out *= phrase.directProbability
            }
            out
        }
    }

    def getSuggestions(input: String): List[Suggestion] = {
        val splitted = input.replaceAll("[,.!?]", "").toLowerCase.split(' ');

        val out = new ArrayBuffer[Suggestion]

        for (i <- 0 until splitted.length) {
            println("i: " + i)
            for (j <- i until splitted.length) {
                print(i + " to " + j + " .... ")

                var string = ""
                for(h <- i to j) {
                    string = string + splitted(h)
                    if (h < j) {
                        string += " "
                    }
                }

                print(";" + string + "; ")

                phrases get(string) match {
                    case None =>
                    case Some(phrasesList) =>
                        for (phrase <- phrasesList) {
                            print(" !dictionary contains:\"" + phrase.translation + "\"! ")
                            out += new Suggestion(phrase.translation,
                                phrase.original,
                                i,
                                j,
                                phrase.directProbability,
                                phrase.lexicalWeighting)
                        }
                }

                println(";")
            }
        }

        out.toList
    }
}
