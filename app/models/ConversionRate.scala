package models

import play.api.libs.json.Json

case class ConversionRate(date: String, source: String, destination: String, amount: Double, result: Double)

object ConversionRate {
  implicit val format = Json.format[ConversionRate]
}