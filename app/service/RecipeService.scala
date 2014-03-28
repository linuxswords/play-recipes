package service

import plugins.CouchDBPlugin
import play.api.libs.json.{JsObject, JsValue, JsArray, Json}
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
      val rev = Json.toJson(revision.get).as[JsObject]
      val recipeWithRevision = Json.toJson(recipe).as[JsObject].deepMerge(rev)
      resolve(db.doc(id.get, recipeWithRevision))
    } else {
      resolve(db.doc(java.util.UUID.randomUUID.toString, Json.toJson(recipe)))
    }
  }

  def delete(id: String, rev: String): Either[scala.Throwable, JsValue] = resolve(db.delete(id, rev))

  def byId(id: String):Either[scala.Throwable, JsValue] = resolve(db.doc(id))
  
  def globalSearch(text: String) :Either[scala.Throwable, JsValue] = {
    val startKey = Json.toJson(text.toLowerCase())
    val endKey = Json.toJson(text.toLowerCase() + "Z")
    resolve(db.view(design, viewGlobalSearch, startKey = Option(startKey), endKey = Option(endKey))) match {
      case Left(t)       => Left(t)
      case Right(result) => Right(result \ "rows")
    }
  }

  def allTitles: Either[scala.Throwable, JsValue] = resolve(db.view(design, viewByTitle)) match {
      case Left(t)       => Left(t)
      case Right(result) => Right(result \ "rows")
    }

  private def resolve[T](future: Future[T]): T = Await.result(future, 5 seconds)
}
