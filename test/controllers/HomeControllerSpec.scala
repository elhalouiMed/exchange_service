package controllers

import models.{AveragePrice, ConversionRate, ErrorMessage, HighestPrice}
import org.hamcrest.core.IsEqual.equalTo
import org.scalatest.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.libs.json.JsObject
import play.api.test._
import play.api.test.Helpers._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 *
 * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
 */
class HomeControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  "HomeController" must {

    "GET referenceByDate for 2022-07-26" in {
      val request = FakeRequest(GET, "/referenceByDate/2022-07-26")
      val home = route(app, request).get

      status(home) mustBe OK
      contentType(home) mustBe Some("application/json")
      val body = contentAsJson(home).as[JsObject]
      (body \ "USD").as[String] must be ("1.0124")
      (body \ "CAD").as[String] must be ("1.3035")
      (body \ "RUB").as[String] must be ("N/A")
    }

    "GET exchangeRate at 2022-07-26 for GBP and CAD" in {
      val request = FakeRequest(GET, "/exchangeRate/2022-07-26/GBP/CAD/20")
      val home = route(app, request).get

      status(home) mustBe OK
      contentType(home) mustBe Some("application/json")
      val body = contentAsJson(home).as[ConversionRate]
      body.date must be ("2022-07-26")
      body.source must be ("GBP")
      body.destination must be ("CAD")
      body.amount must be (20)
      body.result must be (30.83090896189598)
    }

    "GET JPY highest Price between 2022-01-01 and 2022-07-26" in {
      val request = FakeRequest(GET, "/highestPrice/2022-01-01/2022-07-26/JPY")
      val home = route(app, request).get

      status(home) mustBe OK
      contentType(home) mustBe Some("application/json")
      val body = contentAsJson(home).as[HighestPrice]
      body.startDate must be ("2022-01-01")
      body.endDate must be ("2022-07-26")
      body.currency must be ("JPY")
      body.highestPrice must be (125.55)
    }

    "GET JPY averagePrice Price between 2022-01-01 and 2022-07-26" in {
      val request = FakeRequest(GET, "/averagePrice/2022-01-01/2022-07-26/JPY")
      val home = route(app, request).get

      status(home) mustBe OK
      contentType(home) mustBe Some("application/json")
      val body = contentAsJson(home).as[AveragePrice]
      body.startDate must be ("2022-01-01")
      body.endDate must be ("2022-07-26")
      body.currency must be ("JPY")
      body.averagePrice must be (134.94537931034478)
    }

    "GET exchangeRate with invalid date" in {
      val request = FakeRequest(GET, "/exchangeRate/2022-17-26/GBP/CAD/20")
      val home = route(app, request).get

      status(home) mustBe BAD_REQUEST
      contentType(home) mustBe Some("application/json")
      val body = contentAsJson(home).as[ErrorMessage]
      body.message must be ("invalid parameters")
    }

    "GET exchangeRate with invalid currency name" in {
      val request = FakeRequest(GET, "/exchangeRate/2022-07-26/GBP/CID/20")
      val home = route(app, request).get

      status(home) mustBe BAD_REQUEST
      contentType(home) mustBe Some("application/json")
      val body = contentAsJson(home).as[ErrorMessage]
      body.message must be ("invalid parameters")
    }

    "GET highest Price with unavailable currency data" in {
      val request = FakeRequest(GET, "/highestPrice/2022-01-01/2022-07-26/LVL")
      val home = route(app, request).get

      status(home) mustBe UNPROCESSABLE_ENTITY
      contentType(home) mustBe Some("application/json")
      val body = contentAsJson(home).as[ErrorMessage]
      body.message must be ("rates are not available at this dates")
    }

    "GET average Price with unavailable currency data" in {
      val request = FakeRequest(GET, "/averagePrice/2022-01-01/2022-07-26/LVL")
      val home = route(app, request).get

      status(home) mustBe UNPROCESSABLE_ENTITY
      contentType(home) mustBe Some("application/json")
      val body = contentAsJson(home).as[ErrorMessage]
      body.message must be ("rates are not available at this dates")
    }
  }
}
