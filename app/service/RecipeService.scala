package service

import plugins.CouchDBPlugin
import play.api.libs.json.{JsValue, JsArray, Json}
import scala.concurrent.duration._
import scala.concurrent.{Future, Await}
import model.Recipe
import model.serialize.SerializeProvider._

/**
 * @author knm
 */
object RecipeService {
  val db = CouchDBPlugin.db("recipe_test")
  val design = "recipe"
  val viewByTitle = "name"

  def store(recipe: Recipe, id: Option[String], revision: Option[String]) = {
    if (id.isDefined && revision.isDefined) {
      Await.result(db.update(id.get, revision.get, Json.toJson(recipe)), 5 seconds)
    } else {
      Await.result(db.doc(java.util.UUID.randomUUID.toString, Json.toJson(recipe)), 5 seconds)
    }
  }

  def delete(id: String, rev: String): Either[scala.Throwable, JsValue] = Await.result(db.delete(id, rev), 5 seconds)

  def byId(id: String):Either[scala.Throwable, JsValue] = Await.result(db.doc(id), 5 seconds)

  def allTitles: Either[scala.Throwable, JsValue] = Await.result(db.view(design, viewByTitle), 5 seconds) match {
      case Left(t) => Left(t)
      case Right(result )=> Right(result \ "rows")
    }
}
