package controllers

import scala.concurrent.duration._

import play.api._
import play.api.mvc._
import scala.concurrent.Await
import play.api.libs.json.{JsValue, Json}
import plugins.CouchDBPlugin
import model.serialize.SerializeProvider._
import service.RecipeService

object Recipe extends Controller {


  def index = Action { implicit request =>
    Ok(views.html.index(forms.RecipeForm.defaultRecipeForm))
  }

  def submitForm = Action { implicit request =>

    forms.RecipeForm.recipeForm.bindFromRequest.fold(
     formWithError => Ok(views.html.index(formWithError)),
     validRecipe => {

       val body: Option[Map[String, Seq[String]]] = request.body.asFormUrlEncoded
       val safeMap = body.map(m => m.withDefaultValue(Seq()))
       val id = safeMap.get("_id").headOption
       val rev = safeMap.get("_rev").headOption

       RecipeService.store(validRecipe, id, rev) match {
         case Left(t)      => UnprocessableEntity(s"id: $id, rev: $rev, ${t.getMessage}")
         case Right(value) => Ok(value)
       }
     }
    )
  }

  def allTitle = Action { implicit request =>
    RecipeService.allTitles match {
      case Left(t)      => UnprocessableEntity(s"error: ${t.getMessage}")
      case Right(value) => Ok(value)
    }
  }

  def remove(id: String, rev: String) = Action { implicit request =>
    RecipeService.delete(id, rev) match {
      case Left(t)      => UnprocessableEntity(s"id: $id, rev: $rev, ${t.getMessage}")
      case Right(value) => Ok(value)
    }
  }

  def byId(id: String) = Action { implicit request =>
    RecipeService.byId(id) match {
      case Left(t)      => UnprocessableEntity(Json.toJson(List("error", t.toString)))
      case Right(value) => Ok(value)
    }
  }

}