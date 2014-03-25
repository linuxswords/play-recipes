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
  val viewGlobalSearch = "globalsearch"

  def store(recipe: Recipe, id: Option[String], revision: Option[String]) = {
    if (id.isDefined && revision.isDefined) {
      resolve(db.update(id.get, revision.get, Json.toJson(recipe)))
    } else {
      resolve(db.doc(java.util.UUID.randomUUID.toString, Json.toJson(recipe)))
    }
  }

  def delete(id: String, rev: String): Either[scala.Throwable, JsValue] = resolve(db.delete(id, rev))

  def byId(id: String):Either[scala.Throwable, JsValue] = resolve(db.doc(id))
  
  def globalSearch(text: String) :Either[scala.Throwable, JsValue] = {
    val results = resolve(db.view(design, viewGlobalSearch, Some(Json.toJson(text.toLowerCase))))
    // TODO: unique here or in the couchdb?
    results
  }

  def allTitles: Either[scala.Throwable, JsValue] = resolve(db.view(design, viewByTitle)) match {
      case Left(t) => Left(t)
      case Right(result )=> Right(result \ "rows")
    }

  private def resolve[T](future: Future[T]): T = Await.result(future, 5 seconds)
}
