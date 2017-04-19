package controllers

import javax.inject._

import org.joda.time.DateTime
import play.api.Configuration
import play.api.libs.json.{Json, JsValue, Reads, Writes}
import play.api.mvc.{Action, Controller}
import services.PredictionIO

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random


@Singleton
class HomeController @Inject() (configuration: Configuration, predictionIO: PredictionIO)(implicit executionContext: ExecutionContext) extends Controller {

  implicit val dateWrites = Writes.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val dateReads = Reads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")

  lazy val mapboxAccessToken: String = configuration.getString("mapbox.access-token").get

  def index = Action {
    Ok(views.html.index())
  }

  def header = Action {
    Ok(views.html.header())
  }

  def documentationSectionHeader = Action {
    Ok(views.html.documentationSectionHeader())
  }

  def documentationData = Action {
    Ok(views.html.documentationData())
  }

  def documentationModels = Action {
    Ok(views.html.documentationModels())
  }

  def dashboardSectionHeader = Action {
    Ok(views.html.dashboardSectionHeader())
  }

  def dashboardAnalysis = Action {
    Ok(views.html.dashboardAnalysis(mapboxAccessToken))
  }

  def dashboardPrediction = Action {
    Ok(views.html.dashboardPrediction(mapboxAccessToken))
  }

  //    request =>
  //    val eventTime = (request.body \ "eventTime").as[DateTime]
  //    val lat = (request.body \ "lat").as[Double]
  //    val lng = (request.body \ "lng").as[Double]
  //    val temperature = (request.body \ "temperature").as[Double]
  //    val clear = (request.body \ "clear").as[Int]
  //    val fog = (request.body \ "fog").as[Int]
  //    val rain = (request.body \ "rain").as[Int]
  //    val snow = (request.body \ "snow").as[Int]
  //    val hail = (request.body \ "hail").as[Int]
  //    val thunder = (request.body \ "thunder").as[Int]
  //    val tornado = (request.body \ "tornado").as[Int]
  //    val heat = (request.body \ "heat").as[Double]
  //    val windchill = (request.body \ "windchill").as[Double]
  //    val precipitation = (request.body \ "precipitation").as[Double]

  // todo: input checking and error handling
  def predict(eventTime: String, lat: Double, lng: Double, temperature: Double,
              clear: Int, fog: Int, rain: Int, snow: Int, hail: Int, thunder: Int,
              tornado: Int, heat: Double, windchill: Double, precipitation: Double) = Action.async {

    var query = Json.obj(
      "eventTime" -> String,
      "lat" -> lat,
      "lng" -> lng,
      "temperature" -> temperature,
      "clear" -> clear,
      "fog" -> fog,
      "rain" -> rain,
      "snow" -> snow,
      "hail" -> hail,
      "thunder" -> thunder,
      "tornado" -> tornado,
      "head" -> heat,
      "windchill" -> windchill,
      "precipitation" -> precipitation
    )

    predictionIO.predict(query).map { json =>
        Ok(toGeoJson2(json, lat, lng))
      }

//    predictionIO.predict(eventTime, lat, lng, temperature, clear,
//      fog, rain, snow, hail, thunder, tornado, heat, windchill, precipitation)
//      .map { json =>
//        Ok(toGeoJson2(json, lat, lng))
//      }
  }

//  def fakePredict = Action {
//    Ok(
//      Json.obj(
//        "demand" -> Random.nextInt()
//      )
//    )
//  }

  private def toGeoJson2(json: JsValue, lat: Double, lon: Double) = {
    var demand = (json \ "demand").as[Double]

    Json.obj(
      "type" -> "Feature",
      "properties" -> Json.obj(
        "Primary ID" -> 1,
        "demand" -> demand
      ),
      "geometry" -> Json.obj(
        "type" -> "Point",
        "coordinates" -> Json.arr(lon, lat)
      )
    )
  }

  private def toGeoJson(value: Double, lat: Double, lon: Double) = {
    Json.obj(
      "type" -> "Feature",
      "properties" -> Json.obj(
        "Primary ID" -> value,
        "demand" -> Random.nextInt(10)
      ),
      "geometry" -> Json.obj(
        "type" -> "Point",
        "coordinates" -> Json.arr(lon, lat)
      )
    )
  }

  def demand(lng: Double, lat: Double) = Action {
    val points = Seq.fill(50) {
      val newLng = lng + (0.1 * Random.nextDouble()) - 0.05
      val newLat = lat + (0.1 * Random.nextDouble()) - 0.05
      toGeoJson(1, newLat, newLng)
    }

    Ok(
      Json.obj(
        "type" -> "FeatureCollection",
        "crs" -> Json.obj(
          "type" -> "name",
          "properties" -> Json.obj(
            "name" -> "urn:ogc:def:crs:OGC:1.3:CRS84"
          )
        ),
        "features" -> points
      )
    )
  }

}
