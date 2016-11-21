package core

import java.io.File
import java.nio.file.{Paths, Files}
import javax.inject.Singleton

import scala.collection.mutable.{ArrayBuffer, HashMap}
import scala.io.Source

@Singleton
class PhrasesManager {
    println("Phrases manager is being initialized!")
    val phrases : HashMap[String, ArrayBuffer[String]] = new HashMap[String, ArrayBuffer[String]]

    println("Loading file: en-cs.slovnik.txt")
    try {
        val matchingRegex = """^([^\t]+)\t([^\t]+)""".r
        for (line <- Source.fromFile("data/en-cs.slovnik.txt").getLines()) {
            val result = matchingRegex.findFirstMatchIn(line)
            if (result.isDefined) {
                val original = result.get.group(1)
                val translation = result.get.group(2)
                if (phrases.contains(original)) {
                    phrases get(original) match {
                        case None =>
                        case Some(list) =>
                            list += translation
                    }
                } else {
                    val list = new ArrayBuffer[String]
                    list += translation
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
    getSuggestions("I doesn't, know my dog!")

    def getSuggestions(input: String): List[Suggestion] = {
        val splitted = input.replaceAll("[,.!?]", "").split(' ');

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
                    case Some(translationList) =>
                        for (translation <- translationList) {
                            print(" !dictionary contains:\"" + translation + "\"! ")
                            out += new Suggestion(translation, string, i, j)
                        }
                }

                println(";")
            }
        }

        return out.toList
    }
}
