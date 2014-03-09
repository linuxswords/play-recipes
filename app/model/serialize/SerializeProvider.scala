package model.serialize

import play.api.libs.json.Json
import model.{Recipe, Ingredient}
import play.api.data.format.Formatter
import play.api.data.FormError

/**
 * @author knm
 */
object SerializeProvider {
  implicit val ingredientFormatter = Json.format[Ingredient]
  implicit val recipeFormatter = Json.format[Recipe]
}
