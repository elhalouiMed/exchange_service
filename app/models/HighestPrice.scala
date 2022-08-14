package models

import play.api.libs.json.Json

case class HighestPrice(startDate: String, endDate: String, currency: String, highestPrice: Double)

object HighestPrice {
  implicit val format = Json.format[HighestPrice]
}