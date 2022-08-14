package controllers

import javax.inject._
import play.api.mvc._
import play.api.cache.SyncCacheApi
import com.github.tototoshi.csv._
import models.{AveragePrice, ConversionRate, ErrorMessage, HighestPrice, Response}
import play.api.libs.json.Json
import java.time.format.DateTimeFormatter
import java.time.LocalDate
import java.io.File
import scala.util.Try

@Singleton
class HomeController @Inject()(cache: SyncCacheApi, controllerComponents: ControllerComponents) extends AbstractController(controllerComponents) {
  lazy val NOT_AVAILABLE = "N/A"
  val reader = CSVReader.open(new File("data/eurofxref-hist.csv"))
  reader.allWithHeaders().foreach(rate => {
    rate.get("Date").map(cache.set(_,rate))
  })

  def rateByDate(date: String) = Action { implicit request: Request[AnyContent] =>
    val rate: Option[Map[String,String]] = cache.get(date)
    Ok(Json.toJson(rate))
  }

  def convert(date: String, source: String, target: String, amount: Double) = Action { implicit request: Request[AnyContent] =>
    val exchange: Option[Map[String,String]] = cache.get(date)
    (for {
      rate <- exchange
      src <- rate.get(source)
      dest <- rate.get(target)
    } yield {
      if(src.equals(NOT_AVAILABLE) || dest.equals(NOT_AVAILABLE)){
        UnprocessableEntity(Json.toJson(ErrorMessage("rate not available at this date")))
      } else {
        val result = (dest.toDouble / src.toDouble) * amount
        Ok(Json.toJson(ConversionRate(date, source, target, amount, result)))
      }
    }).getOrElse(BadRequest(Json.toJson(ErrorMessage("invalid parameters"))))
  }

  def highestPrice(startDate: String, endDate: String, currency: String)= Action { implicit request =>
    withValidCurrencyPrices(startDate: String, endDate: String, currency: String)  {  prices =>
      Ok(Json.toJson(HighestPrice(startDate, endDate, currency,prices.min)))
    }
  }

  def averagePrice(startDate: String, endDate: String, currency: String)= Action { implicit request =>
    withValidCurrencyPrices(startDate: String, endDate: String, currency: String)  {  prices =>
      val average: Double = (prices.sum / prices.length)
      Ok(Json.toJson(AveragePrice(startDate, endDate, currency, average)))
    }
  }

  private[this] def withDateBetween(startDate: String,endDate: String)(action: List[String] => Result) = {
    def convertToFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val maybeStartDate = Try(LocalDate.parse(startDate,convertToFormat)).toOption
    val maybeEndDate = Try(LocalDate.parse(endDate,convertToFormat)).toOption
    (maybeStartDate,maybeEndDate) match {
      case (Some(start), Some(end)) => {
        val dateRange = start.toEpochDay.until(end.plusDays(1).toEpochDay).map(LocalDate.ofEpochDay).toList
        action(dateRange.map(date => convertToFormat.format(date)).toList)
      }
      case (_,_) => BadRequest(Json.toJson(ErrorMessage("invalid dates - use iso format yyyy-mm-dd")))
    }
  }

  private [this] def getPricesByDate(dates: List[String], currency: String): List[String] = {
    dates.flatMap(date => {
      val exchange: Option[Map[String,String]] = cache.get(date)
      for {
        rate <- exchange
        price <- rate.get(currency)
      } yield price
    })
  }

  private [this] def withValidCurrencyPrices(startDate: String, endDate: String, currency: String)(action: List[Double] => Result) = {
    withDateBetween(startDate: String, endDate: String) { allDates =>
      val prices = getPricesByDate(allDates, currency)
      prices match {
        case List() => BadRequest(Json.toJson(ErrorMessage("invalid parameters")))
        case maybeList if(maybeList.filterNot(_ equals NOT_AVAILABLE).isEmpty) =>
          UnprocessableEntity(Json.toJson(ErrorMessage("rates are not available at this dates")))
        case _ => action(prices.map(_.toDouble))
      }
    }
  }

}
