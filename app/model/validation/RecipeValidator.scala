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

  implicit val ingredientReads: Reads[Ingredient] = (
    (__ \ "amount").read[BigDecimal](min(BigDecimal(0))) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "unit").read[Option[String]]
    )(Ingredient.apply _)

  implicit val recipeReads: Reads[Recipe] = (
    (__ \ "title").read[String](minLength[String](2)) and
      (__ \ "alternateTitle").read[Option[String]] and
      (__ \ "instruction").read[Option[String]] and
      (__ \ "comment").read[Option[String]] and
      (__ \ "ingredients").read[List[Ingredient]] and
      (__ \ "keywords").read[Option[List[String]]] and
      (__ \ "timeRequired").read[Option[BigDecimal]] and
      (__ \ "url").read[Option[String]]
    )(Recipe.apply _)

}
