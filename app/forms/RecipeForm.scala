package forms

import play.api.data._
import play.api.data.Forms._
import model.{Recipe, Ingredient}
import java.net.URL

/**
 * @author knm
 */
object RecipeForm {

  val ingredientMapping = mapping(
    "quantity" -> bigDecimal(10,2),
    "name" -> nonEmptyText,
    "unit" -> optional(text)
  )(Ingredient.apply)(Ingredient.unapply)


  val recipeForm = Form(
    mapping(
      "title" -> nonEmptyText,
      "alternativeTitle" -> optional(text),
      "comment" -> optional(text),
      "ingredients" -> list(ingredientMapping),
      "keywords" -> optional(text),
      "timeRequired" -> optional(bigDecimal(10,2)),
      "url" -> optional(text)
    )(Recipe.formApply)(Recipe.formUnapply)
  )

  val defaultRecipeForm = recipeForm.fill(Recipe.emptyRecipe)

}
