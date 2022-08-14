package models

import play.api.libs.json.Json

case class AveragePrice(startDate: String, endDate: String, currency: String, averagePrice: Double)

object AveragePrice {
  implicit val format = Json.format[AveragePrice]
}