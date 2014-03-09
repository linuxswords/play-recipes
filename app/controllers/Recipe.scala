package controllers

import scala.concurrent.duration._

import play.api._
import play.api.mvc._
import scala.concurrent.Await
import play.api.libs.json.Json
import plugins.CouchDBPlugin
import model.serialize.SerializeProvider._
import service.RecipeService

object Recipe extends Controller {


  def index = Action {
    Ok(views.html.recipeForm(forms.RecipeForm.defaultRecipeForm))
  }

  def submitForm = Action { implicit request =>
    forms.RecipeForm.recipeForm.bindFromRequest.fold(
     formWithError => Ok(views.html.recipeForm(formWithError)),
     validRecipe => {
       val result = RecipeService.store(validRecipe)
       Redirect(routes.Recipe.index.url)
     }
    )
  }

  def allTitle = Action { implicit request =>
    RecipeService.allTitles match {
      case Left(_) => Ok("failed")
      case Right(titles) => Ok(titles)
    }
  }

}