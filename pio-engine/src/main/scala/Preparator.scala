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

import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.api.ndarray.INDArray

import java.io._

class PreparedData(
  //val dataSet: DataSet,
  val dataSet: INDArray, // inputs array
) extends Serializable
//     with SanityCheck {


//   override def sanityCheck(): Unit = {
//     require(dataSet.take(1).nonEmpty, s"data cannot be empty!")
//   }
// }

class Preparator extends PPreparator[TrainingData, PreparedData] {

  @transient lazy val logger = Logger[this.type]

  def prepare(sc: SparkContext, trainingData: TrainingData): PreparedData = {


    // clustering coordinates and assign cluster labels.
    val standardScaler = new StandardScaler(true, true)//Used to calculate mean and std so that we can normalize features
    val locationData = trainingData.data map {entry => Vectors.dense(entry.lat, entry.lng)} distinct() cache()

     // store them statically so that we can use them when querying
    Preparator.locationClusterModel = Some(KMeans.train(locationData, Preparator.numOfClustersForLocationModel,
      Preparator.numOfIterationsForLocationModel))

    val normalizedTime = KooberUtil.createNormalizedMap(trainingData.data)

    val countMap = normalizedTime.values.countByValue()
    val normalizedTimeMap = normalizedTime.collectAsMap()

    val featureVector = trainingData.data map { trainingDataEntry =>
      Preparator.toFeaturesVector(trainingDataEntry.eventTime, trainingDataEntry.lat, trainingDataEntry.lng)
    } cache ()

    // store them statically so that we can normalize query data during query time
    Preparator.standardScalerModel = Some(standardScaler.fit(featureVector))

    val data = trainingData.data map { trainingDataEntry =>
      // Label transformation
      val demand = countMap.get(normalizedTimeMap.get(trainingDataEntry.eventTime).get).get
      
      // Feature transformation
      val timeFeatureVector = Preparator.toFeaturesVector(trainingDataEntry.eventTime, trainingDataEntry.lat, trainingDataEntry.lng)
      val normalizedTimeFeatureVector = Preparator.standardScalerModel.get.transform(timeFeatureVector)
      val predictedLocationLabel = Preparator.locationClusterModel.get.predict(Vectors.dense(trainingDataEntry.lat, trainingDataEntry.lng))
      val finalFeatures = Preparator.toFeaturesVector(normalizedTimeFeatureVector, predictedLocationLabel)

      Array [Double] (demand) ++ (finalFeatures.toArray)

    } cache ()

    val dataArray : Array[Array[Double]] = data.collect().toArray
    val features = dataArray.transpose.tail.transpose
    val labels = dataArray.transpose.head

    val featuresINDArray = Nd4j.create(features)
    println(featuresINDArray.size(0) + " " + featuresINDArray.size(1))

    val labelsINDArray = labelsArray.asNDArray(labels.length,1)
    println(labelsINDArray.size(0) + " " + labelsINDArray.size(1))

    var dataset = new DataSet(featuresINDArray, labelsINDArray)
    var inputINDArray = Nd4j.create(featuresArray)

    new PreparedData(inputINDArray)
  }
}


object Preparator {

  @transient lazy val logger = Logger[this.type]
  var locationClusterModel: Option[KMeansModel] = None
  var standardScalerModel: Option[StandardScalerModel] = None
  val numOfClustersForLocationModel = 5
  val numOfIterationsForLocationModel = 100

  def toFeaturesVector(eventTime: DateTime, lat: Double, lng: Double): Vector = {
    Vectors.dense(Array(
      eventTime.dayOfWeek().get().toDouble,
      eventTime.dayOfMonth().get().toDouble,
      eventTime.minuteOfDay().get().toDouble,
      eventTime.monthOfYear().get().toDouble
    ))
  }

  def toFeaturesVector(normalizedFeatureVector: Vector, locationClusterLabel: Int): Vector = {
    val timeFeatures = normalizedFeatureVector.toArray
    val locationFeatureOneHotEncoding = KooberUtil.convertIntToBinaryArray(locationClusterLabel, numOfClustersForLocationModel)
    Vectors.dense(timeFeatures ++ locationFeatureOneHotEncoding)
  }
}