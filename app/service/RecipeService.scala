package service

import plugins.CouchDBPlugin
import play.api.libs.json.{JsArray, Json}
import scala.concurrent.duration._
import scala.concurrent.Await
import model.Recipe
import model.serialize.SerializeProvider._

/**
 * @author knm
 */
object RecipeService {
  val db = CouchDBPlugin.db("recipe_test")
  val design = "recipe"
  val viewByTitle = "name"

  def store(recipe: Recipe) = Await.result(db.doc(java.util.UUID.randomUUID.toString, Json.toJson(recipe)), 5 seconds)

  def byId(id: String) = Await.result(db.doc(id), 5 seconds)

  def allTitles = {

    val result = Await.result(db.view(design, viewByTitle), 5 seconds)
    result \ "rows"
  }

}
