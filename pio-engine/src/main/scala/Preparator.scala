package edu.cs5152.predictionio.demandforecasting


import grizzled.slf4j.Logger
import org.apache.predictionio.controller.{PPreparator, SanityCheck}
import org.apache.spark.SparkContext
import org.apache.spark.mllib.clustering.{KMeans, KMeansModel}
import org.apache.spark.mllib.feature.{StandardScaler, StandardScalerModel}
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.joda.time.DateTime

class PreparedData(
    val data: RDD[LabeledPoint]
) extends Serializable
    with SanityCheck {


  override def sanityCheck(): Unit = {
    require(data.take(1).nonEmpty, s"data cannot be empty!")
  }
}

class Preparator extends PPreparator[TrainingData, PreparedData] {

  @transient lazy val logger = Logger[this.type]

  def prepare(sc: SparkContext, trainingData: TrainingData): PreparedData = {

    // clustering coordinates and assign cluster labels.
    val standardScaler = new StandardScaler(true, true)//Used to calculate mean and std so that we can normalize features
<<<<<<< HEAD
    val locationData = trainingData.data map {entry => Vectors.dense(entry.lat, entry.lng)} distinct() cache()
=======
    val locationData = trainingData.data map {entry => Vectors.dense(entry.lat, entry.lng)}
>>>>>>> 581b39ed71f2f0552c1b1e87830b54fccc87e57a

    // store them statically so that we can use them when querying
    Preparator.locationClusterModel = Some(KMeans.train(locationData, Preparator.numOfClustersForLocationModel,
      Preparator.numOfIterationsForLocationModel))

<<<<<<< HEAD
    val normalizedTime = KooberUtil.createNormalizedMap(trainingData.data)

    val countMap = normalizedTime.values.countByValue()
    val normalizedTimeMap = normalizedTime.collectAsMap()

    val featureVector = trainingData.data map { trainingDataEntry =>
      Preparator.toFeaturesVector(trainingDataEntry.eventTime, trainingDataEntry.lat, trainingDataEntry.lng)
    } cache ()
=======
    val timeAndLatLng = trainingData.data map {entry => (entry.eventTime, entry.lat, entry.lng)} distinct()
    val timeAndLocationLabels = timeAndLatLng map {
      entry => (entry._1, Preparator.locationClusterModel.get.predict(Vectors.dense(entry._2, entry._3)))
    } cache()

    val normalizedTimeAndLocationLabels = KooberUtil.createNormalizedTimeAndLocationLabelTuple(timeAndLocationLabels)
    val countMap = normalizedTimeAndLocationLabels.countByValue()

    val timeToWeatherMap = trainingData.data map { trainingDataEntry =>
      (KooberUtil.normalize(trainingDataEntry.eventTime), (trainingDataEntry.temperature, trainingDataEntry.clear, trainingDataEntry.fog, trainingDataEntry.rain,
        trainingDataEntry.snow, trainingDataEntry.hail, trainingDataEntry.thunder, trainingDataEntry.tornado))
    } collectAsMap()

    val featureVector = normalizedTimeAndLocationLabels map { entry =>
      val weatherDataTuple = timeToWeatherMap.get(entry._1).get
      val denormalizedEventTime = KooberUtil.denormalize(entry._1)
      Preparator.toFeaturesVector(denormalizedEventTime, weatherDataTuple._1,
        weatherDataTuple._2, weatherDataTuple._3, weatherDataTuple._4, weatherDataTuple._5, weatherDataTuple._6,
        weatherDataTuple._7, weatherDataTuple._8)
    } cache()
>>>>>>> 581b39ed71f2f0552c1b1e87830b54fccc87e57a

    // store them statically so that we can normalize query data during query time
    Preparator.standardScalerModel = Some(standardScaler.fit(featureVector))

<<<<<<< HEAD
    val data = trainingData.data map { trainingDataEntry =>
      val demand = countMap.get(normalizedTimeMap.get(trainingDataEntry.eventTime).get).get
      val timeFeatureVector = Preparator.toFeaturesVector(trainingDataEntry.eventTime, trainingDataEntry.lat, trainingDataEntry.lng)
      val normalizedTimeFeatureVector = Preparator.standardScalerModel.get.transform(timeFeatureVector)
      val predictedLocationLabel = Preparator.locationClusterModel.get.predict(Vectors.dense(trainingDataEntry.lat, trainingDataEntry.lng))
      LabeledPoint(demand, Preparator.toFeaturesVector(normalizedTimeFeatureVector, predictedLocationLabel))
=======
    val data = normalizedTimeAndLocationLabels map { entry =>
      val demand = countMap.get(entry).get
      val weatherDataTuple = timeToWeatherMap.get(entry._1).get
      val denormalizedEventTime = KooberUtil.denormalize(entry._1)
      val timeFeatureVector = Preparator.toFeaturesVector(denormalizedEventTime, weatherDataTuple._1,
        weatherDataTuple._2, weatherDataTuple._3, weatherDataTuple._4, weatherDataTuple._5, weatherDataTuple._6,
        weatherDataTuple._7, weatherDataTuple._8)
      val normalizedTimeFeatureVector = Preparator.standardScalerModel.get.transform(timeFeatureVector)
      LabeledPoint(demand, Preparator.combineFeatureVectors(normalizedTimeFeatureVector, entry._2))
>>>>>>> 581b39ed71f2f0552c1b1e87830b54fccc87e57a
    } cache ()

    new PreparedData(data)
  }
}

object Preparator {

  @transient lazy val logger = Logger[this.type]
  var locationClusterModel: Option[KMeansModel] = None
  var standardScalerModel: Option[StandardScalerModel] = None
<<<<<<< HEAD
  val numOfClustersForLocationModel = 5
  val numOfIterationsForLocationModel = 100

  def toFeaturesVector(eventTime: DateTime, lat: Double, lng: Double): Vector = {
=======
  val numOfClustersForLocationModel = 200
  val numOfIterationsForLocationModel = 100

  def toFeaturesVector(eventTime: DateTime, temperature: Double, clear: Int, fog: Int, rain: Int, snow: Int, hail: Int, thunder: Int, tornado: Int): Vector = {
>>>>>>> 581b39ed71f2f0552c1b1e87830b54fccc87e57a
    Vectors.dense(Array(
      eventTime.dayOfWeek().get().toDouble,
      eventTime.dayOfMonth().get().toDouble,
      eventTime.minuteOfDay().get().toDouble,
<<<<<<< HEAD
      eventTime.monthOfYear().get().toDouble
    ))
  }

  def toFeaturesVector(normalizedFeatureVector: Vector, locationClusterLabel: Int): Vector = {
=======
      eventTime.monthOfYear().get().toDouble,
      temperature,
      clear.toDouble,
      fog.toDouble,
      rain.toDouble,
      snow.toDouble,
      hail.toDouble,
      thunder.toDouble,
      tornado.toDouble
    ))
  }

  def combineFeatureVectors(normalizedFeatureVector: Vector, locationClusterLabel: Int): Vector = {
>>>>>>> 581b39ed71f2f0552c1b1e87830b54fccc87e57a
    val timeFeatures = normalizedFeatureVector.toArray
    val locationFeatureOneHotEncoding = KooberUtil.convertIntToBinaryArray(locationClusterLabel, numOfClustersForLocationModel)
    Vectors.dense(timeFeatures ++ locationFeatureOneHotEncoding)
  }
}
