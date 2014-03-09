package model

import java.net.URL
import Ingredient._

/**
 *
 * @param name name
 * @param unit unit like teaspoon, gramm, kg
 * @param amount amount
 */
case class Ingredient(amount: BigDecimal, name: String, unit: Option[String])

object Ingredient{
  val emptyIngredient = Ingredient(1, "", None)
}

/**
 *
 * @param title title
 * @param alternateTitle optional title
 * @param comment optional comment
 * @param ingredients list of ingredients
 * @param keywords keywords or tags
 * @param timeRequired time to prepare the meal in minutes
 * @param url optional url as a source
 */
case class Recipe(title: String,
                  alternateTitle: Option[String],
                  comment: Option[String],
                  ingredients: List[Ingredient],
                  keywords: Option[List[String]],
                  timeRequired: Option[BigDecimal],
                  url: Option[String])

object Recipe{
  val emptyRecipe = Recipe("", None, None, List(emptyIngredient), None, None, None)

  def formApply(title: String,
  alternateTitle: Option[String],
  comment: Option[String],
  ingredients: List[Ingredient],
  keywords: Option[String],
  timeRequired: Option[BigDecimal],
  url: Option[String]): Recipe = Recipe(
      title, alternateTitle, comment, ingredients, keywords.map(_.split(" ")).map(_.toList), timeRequired, url)

  def formUnapply(recipe: Recipe) = {
    Option( ( recipe.title, recipe.alternateTitle, recipe.comment, recipe.ingredients, Option(recipe.keywords.mkString(" ")), recipe.timeRequired, recipe.url ) )
  }
}