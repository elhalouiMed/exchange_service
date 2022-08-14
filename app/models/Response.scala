package models

import play.api.libs.json.Json

case class Response(result: Double)

object Response {
  implicit val format = Json.format[Response]
}