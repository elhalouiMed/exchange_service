package models

import play.api.libs.json.Json

case class ErrorMessage( message: String )

object ErrorMessage {
  implicit val format = Json.format[ErrorMessage]
}