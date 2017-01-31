package controllers

import javax.inject.Inject

import core.PhrasesManager
import play.api.libs.json.Json
import play.api.mvc._
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.io.Source

class Application @Inject() (val phrasesManager: PhrasesManager) extends Controller {
    def index = Action {
        Ok(views.html.index())
    }

    def test = Action {
        val filePath = "data/test-data-1.json"
        val json = Json.parse(Source.fromFile("public/" + filePath).mkString);
        Ok(views.html.test(filePath, json.toString()))
    }

    def getSuggestions(sentence: String) = Action.async {
        phrasesManager.getSuggestions(sentence).map(suggestions => Ok(Json.toJson(suggestions)))
    }
}