package controllers

import javax.inject.Inject

import core.{Suggestion, PhrasesManager}
import core.Suggestion._
import models.PhrasesDAO
import play.api._
import play.api.libs.json.{JsPath, Writes, Json}
import play.api.mvc._
import scala.concurrent._
import ExecutionContext.Implicits.global

class Application @Inject() (val phrasesManager: PhrasesManager, val phrasesDao: PhrasesDAO) extends Controller {

    def index = Action.async {
        val test = phrasesDao.retrieveByOriginal("dog")
        test.map(phrases => {
            val x = 0
            Ok(views.html.index("Your new application is ready."))
        })
    }

    def getSuggestions(sentence: String) = Action {
        val suggestions = phrasesManager.getSuggestions(sentence)
        Ok(Json.toJson(suggestions))
    }
}