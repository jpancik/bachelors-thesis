package controllers

import javax.inject.Inject

import core.PhrasesManager
import play.api.libs.json.Json
import play.api.mvc._
import scala.concurrent._
import ExecutionContext.Implicits.global

class Application @Inject() (val phrasesManager: PhrasesManager) extends Controller {
    def index = Action {
        Ok(views.html.index("Your new application is ready."))
    }

    def getSuggestions(sentence: String) = Action.async {
        phrasesManager.getSuggestions(sentence).map(suggestions => Ok(Json.toJson(suggestions)))
    }
}