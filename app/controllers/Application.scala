package controllers

import javax.inject.Inject

import core.PhrasesManager
import forms.ParseLinesForm
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent._
import ExecutionContext.Implicits.global
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
                val lines = form.split('.')
                Ok(views.html.translator(lines.map(line => {
                    line + "."
                })))
            }
        )
    }

    def test = Action {
        val filePath = "data/test-data-wikipedia.500.json"
        val json = Json.parse(Source.fromFile("public/" + filePath).mkString);
        Ok(views.html.test(filePath, json.toString()))
    }

    def getSuggestions(sentence: String) = Action.async {
        phrasesManager.getSuggestions(sentence).map(suggestions => Ok(Json.toJson(suggestions)))
    }

    // Not used, development.
    def old = Action {
        Ok(views.html.old())
    }
}