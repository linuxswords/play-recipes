package model.validation

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._
import play.api.libs.functional.syntax._
import model.{Recipe, Ingredient}

/**
 * @author knm
 */
object RecipeValidator {

  implicit val ingredientWrites: Writes[Ingredient] = (
    (__ \ "amount").write[BigDecimal] and
      (__ \ "name").write[String] and
      (__ \ "unit").write[Option[String]]
    )(unlift(Ingredient.unapply))

  implicit val ingredientReads: Reads[Ingredient] = (
    (__ \ "amount").read[BigDecimal](min(BigDecimal(0))) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "unit").read[Option[String]]
    )(Ingredient.apply _)

  implicit val recipewrites: Writes[Recipe] = (
    (__ \ "title").write[String] and
      (__ \ "alternateTitle").write[Option[String]] and
      (__ \ "instruction").write[Option[String]] and
      (__ \ "comment").write[Option[String]] and
      (__ \ "ingredients").write[List[Ingredient]] and
      (__ \ "keywords").write[Option[List[String]]] and
      (__ \ "timeRequired").write[Option[BigDecimal]] and
      (__ \ "url").write[Option[String]]
    )(unlift(Recipe.unapply))

  implicit val recipereads: Reads[Recipe] = (
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
