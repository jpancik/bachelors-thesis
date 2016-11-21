package core

import play.api.libs.json._

case class Suggestion(val translation: String, val original: String, val indexStart: Int, val indexEnd: Int)

object Suggestion {
    implicit val suggestionFormat = Json.writes[Suggestion]

//    implicit val suggestionWrites = new Writes[Suggestion] {
//        def writes(suggestion: Suggestion): JsValue = {
//            Json.obj(
//                "translation" -> suggestion.translation,
//                "original" -> suggestion.original,
//                "indexStart" -> suggestion.indexStart.toString,
//                "indexEnd" -> suggestion.indexEnd.toString
//            )
//        }
//    }
}


