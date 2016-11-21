package controllers

import javax.inject.Inject

import core.{Suggestion, PhrasesManager}
import core.Suggestion._
import play.api._
import play.api.libs.json.{JsPath, Writes, Json}
import play.api.mvc._

class Application @Inject() (val phrasesManager: PhrasesManager) extends Controller {

    def index = Action {
        Ok(views.html.index("Your new application is ready."))
    }

    def getSuggestions(sentence: String) = Action {
        val suggestions = phrasesManager.getSuggestions(sentence)
        Ok(Json.toJson(suggestions))
    }
}