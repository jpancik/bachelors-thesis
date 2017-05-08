package controllers

import javax.inject.Inject

import core.PhrasesManager
import forms.ParseLinesForm
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc._

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.collection.mutable.ArrayBuffer
import scala.io.Source

class Application @Inject() (val phrasesManager: PhrasesManager) extends Controller {
    def index = Action {
        Ok(views.html.index())
    }

    def inputLines = Action {
        Ok(views.html.inputLines())
    }

    def parseLines = Action { implicit request =>
        ParseLinesForm.form.bindFromRequest().fold(
            hasErrors = form => Redirect(routes.Application.inputLines()),
            success = form => {
                // Parse input sentence by sentences.
                val lines = new ArrayBuffer[String]
                var index: Int = 0
                var encountered: Boolean = false
                val builder = new StringBuilder()
                while (index < form.length) {
                    if(form.charAt(index) == '.' || form.charAt(index) == '?' || form.charAt(index) == '!') {
                        encountered = true
                    } else {
                        if (encountered && form.charAt(index) != ' ') {
                            encountered = false
                            lines += builder.toString
                            builder.clear()
                        }
                    }

                    builder.append(form.charAt(index))
                    index += 1
                }
                if (builder.nonEmpty) {
                    lines += builder.toString
                }

                Ok(views.html.translator(lines.toList))
            }
        )
    }

    def test = Action {
        //val json = Json.parse(Source.fromFile("public/" + filePath).mkString);
        Ok(views.html.test())
    }

    def getSuggestions(sentence: String) = Action.async {
        phrasesManager.getSuggestions(sentence).map(suggestions => Ok(Json.toJson(suggestions)))
    }

    // Not used, development.
    def old = Action {
        Ok(views.html.old())
    }
}