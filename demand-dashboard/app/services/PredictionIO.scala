package services

import javax.inject.{Inject, Singleton}

import org.joda.time.DateTime
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSClient
import scala.concurrent.{ExecutionContext, Future}

trait PredictionIO {
    def predict(json: JsValue) : Future[JsValue]
//  def predict(eventTime: DateTime, lat: Double, lng: Double, temperature: Double,
//    clear: Int, fog: Int, rain: Int, snow: Int, hail: Int, thunder: Int,
//    tornado: Int, heat: Double, windchill: Double, precipitation: Double)
//    : Future[JsValue]
}

@Singleton
class PredictionIOImpl @Inject() (configuration: Configuration, wsClient: WSClient)(implicit executionContext: ExecutionContext) extends PredictionIO {

  val predictionIOUrl = configuration.getString("predictionio.url").get
//
//  override def predict(eventTime: String, lat: Double, lng: Double, temperature: Double,
//                        clear: Int, fog: Int, rain: Int, snow: Int, hail: Int, thunder: Int,
//                        tornado: Int, heat: Double, windchill: Double, precipitation: Double)
//                        : Future[JsValue] = {

  override def predict(json: JsValue) : Future[JsValue] = {
    print(predictionIOUrl)
    wsClient.url(predictionIOUrl).post(json)
      .map { response =>
        println(response.body)
        response.json
      }
  }
}

//      "eventTime" -> eventTime,
//      "lat" -> lat,
//      "lng" -> lng,
//      "temperature" -> temperature,
//      "clear" -> clear,
//      "fog" -> fog,
//      "rain" -> rain,
//      "snow" -> snow,
//      "hail" -> hail,
//      "thunder" -> thunder,
//      "tornado" -> tornado,
//      "head" -> heat,
//      "windchill" -> windchill,
//      "precipitation" -> precipitation).get()
