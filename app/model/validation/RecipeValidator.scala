package model.validation

import model.serialize.SerializeProvider._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import model.{Recipe, Ingredient}

/**
 * @author knm
 */
object RecipeValidator {

  implicit val ingredientReads: Format[Ingredient] = (
    (__ \ "amount").format[BigDecimal](min(BigDecimal(0))) and
      (__ \ "name").format[String](minLength[String](2)) and
      (__ \ "unit").format[Option[String]]
    )(Ingredient.apply _)

  implicit val recipeformats: Format[Recipe] = (
    (__ \ "title").format[String](minLength[String](2)) and
      (__ \ "alternateTitle").format[Option[String]] and
      (__ \ "instruction").format[Option[String]] and
      (__ \ "comment").format[Option[String]] and
      (__ \ "ingredients").format[List[Ingredient]] and
      (__ \ "keywords").format[Option[List[String]]] and
      (__ \ "timeRequired").format[Option[BigDecimal]] and
      (__ \ "url").format[Option[String]]
    )(Recipe.apply _)

}
