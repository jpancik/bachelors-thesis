package models

import javax.inject.{Inject, Singleton}

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile
import slick.driver.PostgresDriver.api._

import scala.concurrent.Future

case class Phrase(original: String, translation: String, directProbability: String, lexicalWeighting: String)

trait PhrasesComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>
    class Phrases(tag: Tag) extends Table[Phrase](tag, "phrases") {
        def * = (original, translation, directProbability, lexicalWeighting) <> (Phrase.tupled, Phrase.unapply)

        def original = column[String]("original")

        def translation = column[String]("translation")

        def directProbability = column[String]("directProbability")

        def lexicalWeighting = column[String]("lexicalWeighting")
    }
}

@Singleton()
class PhrasesDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends PhrasesComponent
        with HasDatabaseConfigProvider[JdbcProfile] {
    val phrases = TableQuery[Phrases]

    def retrieveByOriginal(original: String): Future[Seq[Phrase]] = db.run(phrases.filter(_.original === original).result)
}