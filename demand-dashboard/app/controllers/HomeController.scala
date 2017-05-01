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
              clear: Int, fog: Int, rain: Int, snow: Int, hail: Int, thunder: Int, tornado: Int) = Action.async {

    var lngLatArray = makeCluster(lat, lng)
//    var resultSeq = new ListBuffer[Future[JsValue]](lngLatArray(0).length)

    val resultSeq = (0 to (lngLatArray(0).length - 1)).map(i => {
        var query = Json.obj(
          "eventTime" -> eventTime,
          "lat" -> lngLatArray(0)(i),
          "lng" -> lngLatArray(1)(i),
          "temperature" -> temperature,
          "clear" -> clear,
          "fog" -> fog,
          "rain" -> rain,
          "snow" -> snow,
          "hail" -> hail,
          "thunder" -> thunder,
          "tornado" -> tornado
        )
        print("hi")
        print(query)
        var prediction = predictionIO.predict(query)
        prediction.map{json => toGeoJson2(json, lngLatArray(0)(i), lngLatArray(1)(i), i) }

    })

    var result = Future.sequence(resultSeq)
    result.map{r => Ok(toGeoJsonCollection(r))}

//    for ( i <- 0 to (lngLatArray(0).length - 1)) {
//      var query = Json.obj(
//        "eventTime" -> eventTime,
//        "lat" -> lngLatArray(0)(i),
//        "lng" -> lngLatArray(1)(i),
//        "temperature" -> temperature,
//        "clear" -> clear,
//        "fog" -> fog,
//        "rain" -> rain,
//        "snow" -> snow,
//        "hail" -> hail,
//        "thunder" -> thunder,
//        "tornado" -> tornado
//      )
//      print("hi")
//      print(query)
//      var prediction = predictionIO.predict(query)
////      predictionIO.predict(query).map { json =>
////        Ok(toGeoJson2(json, lat, lng))
////      }
//      resultSeq(i) = prediction.map{json => toGeoJson2(json, lngLatArray(0)(i), lngLatArray(1)(i), i)}
//
//    }
//    var result = Future.sequence(resultSeq)
//    resuls.map{r => Ok(toGeoJsonCollection(r))}


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

  private def toGeoJson2(json: JsValue, lat: Double, lon: Double, id: Int) = {
    var demand = (json \ "demand").as[Double]

    Json.obj(
      "type" -> "Feature",
      "properties" -> Json.obj(
        "Primary ID" -> id,
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

  private def toGeoJsonCollection(demands: IndexedSeq[JsValue]) = {
    print(demands)
    Json.obj(
      "type" -> "FeatureCollection",
      "features" -> demands
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


  def makeCluster(lat:Double, lng:Double) : Array[Array[Double]] = {
    var lats = Array(lat, lat + 2*0.0016, lat + 0.0016, lat + 0.0016, lat + 0.0016, lat, lat, lat, lat, lat - 0.0016, lat - 0.0016, lat - 0.0016, lat - 2*0.0016)
    var lngs = Array(lng, lng, lng - 0.0016, lng, lng + 0.0016, lng - 2*0.0016, lng - 0.0016, lng + 0.0016, lng + 2*0.0016, lng - 0.0016, lng, lng + 0.0016, lng)
    Array(lats, lngs)
  }

}
