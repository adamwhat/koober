import java.time.{Duration, ZonedDateTime}

import akka.NotUsed
import akka.stream.scaladsl.Source
import org.joda.time.DateTime
import play.api.libs.json.{JsObject, Json}

import scala.collection.immutable.Iterable
import scala.util.Random

object FakeDataSource {

  val baseLat: Double = 40.7550506592
  val baseLng: Double = -73.96534729

  def randomJson(dateTime: ZonedDateTime): JsObject = {
    val randomLat = baseLat + (Random.nextDouble() - 0.5)
    val randomLng = baseLng + (Random.nextDouble() - 0.5)
<<<<<<< HEAD

    Json.obj(
        "lngLat" -> Json.obj(
          "lat" -> randomLat,
          "lng" -> randomLng
=======
    val randomTemperature = Random.nextDouble()
    val randomClear = 1
    val randomFog = 0
    val randomRain = 0
    val randomSnow = 0
    val randomHail = 0
    val randomThunder = 0
    val randomTornado = 0
    val randomHeat = Random.nextDouble()
    val randomWindchill = Random.nextDouble()
    val randomPrecipitation = Random.nextDouble()

    Json.obj(
        "properties" -> Json.obj(
          // Location Properties
          "lat" -> randomLat,
          "lng" -> randomLng,
          // Weather Properties
          "temperature" -> randomTemperature,
          "clear" -> randomClear,
          "fog" -> randomFog,
          "rain" -> randomRain,
          "snow" -> randomSnow,
          "hail" -> randomHail,
          "thunder" -> randomThunder,
          "tornado" -> randomTornado,
          "heat" -> randomHeat,
          "windchill" -> randomWindchill,
          "precipitation" -> randomPrecipitation
>>>>>>> 581b39ed71f2f0552c1b1e87830b54fccc87e57a
        ),
        "status" -> "pickup",
        "datetime" -> new DateTime(dateTime.toEpochSecond * 1000)
    )
  }

  def apply(numRecords: Int, startDate: ZonedDateTime, endDate: ZonedDateTime, numClusters: Int, demandDistPerCluster: Int): Source[JsObject, NotUsed] = {

    val timeBetween = Duration.between(startDate, endDate)

    val numBaseRecords = (numRecords * 0.20).toInt

    // 20% of numRecords get random dates
    val baseRandomIterable = Iterable.fill(numBaseRecords) {
      val randomSecondsToAddToStart: Long = (timeBetween.getSeconds * Random.nextDouble()).toLong
      val randomZonedDateTime = startDate.plusSeconds(randomSecondsToAddToStart)
      randomJson(randomZonedDateTime)
    }

    val baseRandomSource = Source[JsObject](baseRandomIterable)

    // 80% of numRecords get clustered with the same date and location
    // todo: is this correctly partitioning?
    val numClusteredRecords = numRecords - numBaseRecords
    val numRecordsPerCluster = (numClusteredRecords.toFloat / numClusters).ceil.toInt
    val clustersIterator = Iterator.fill(numClusteredRecords)(Unit).grouped(numRecordsPerCluster).flatMap { partition =>
      val randomSecondsToAddToStart: Long = (timeBetween.getSeconds * Random.nextDouble()).toLong
      val randomZonedDateTime = startDate.plusSeconds(randomSecondsToAddToStart)
      val json = randomJson(randomZonedDateTime)
      partition.map(_ => json)
    }

    val clusteredSource = Source.fromIterator(() => clustersIterator)

    baseRandomSource ++ clusteredSource
  }

}
