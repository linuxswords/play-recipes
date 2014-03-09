package controllers

import scala.concurrent.duration._

import play.api._
import play.api.mvc._
import scala.concurrent.Await
import play.api.libs.json.Json
import plugins.CouchDBPlugin
import model.serialize.SerializeProvider._

object Recipe extends Controller {

  def index = Action {
    Ok(views.html.recipeForm(forms.RecipeForm.defaultRecipeForm))
  }

  def submitForm = Action { implicit request =>

    forms.RecipeForm.recipeForm.bindFromRequest.fold(
     formWithError => Ok(views.html.recipeForm(formWithError)),
     validRecipe => {
       val result = Await.result(CouchDBPlugin.db("recipe_test").doc(java.util.UUID.randomUUID.toString, Json.toJson(validRecipe)), 5 seconds)
       Redirect(routes.Recipe.index.url)
     }
    )


  }

}