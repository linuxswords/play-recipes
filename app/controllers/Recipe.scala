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

       val id = request.body.asFormUrlEncoded.get("_id").headOption
       val rev = request.body.asFormUrlEncoded.get("_rev").headOption

       val result = RecipeService.store(validRecipe, id, rev)
       Redirect(routes.Recipe.index.url)
     }
    )
  }

  def allTitle = Action { implicit request =>
    Ok(RecipeService.allTitles)
  }

  def byId(id: String) = Action { implicit request =>
    RecipeService.byId(id) match {
      case Left(t)      => Ok(Json.toJson(List("error", t.toString)))
      case Right(value) => Ok(value)
    }
  }

}