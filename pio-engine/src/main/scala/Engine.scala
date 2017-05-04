package edu.cs5152.predictionio.demandforecasting

import org.apache.predictionio.controller.{Engine, EngineFactory}
import org.joda.time.DateTime

class Query(
             val eventTime: String,
             val lng: Double,
             val lat: Double,
             val temperature: Double,
             val clear: Int,
             val fog: Int,
             val rain: Int,
             val snow: Int,
             val hail: Int,
             val thunder: Int,
             val tornado: Int

) extends Serializable

class PredictedResult(
                       val demand: Double
                     ) extends Serializable

class ActualResult(
                    val demand: Double
                  ) extends Serializable

object ForecastingEngine extends EngineFactory {
  def apply() = {
    new Engine(
      classOf[DataSource],
      classOf[Preparator],
<<<<<<< HEAD
      Map("randomForest" -> classOf[RandomForestAlgorithm],
          "algRegression" -> classOf[Algorithm],
          "algGBTree"     -> classOf[AlgorithmGBTree],
          "multinomialLogistic" -> classOf[MultinomialLogisticRegressionAlgorithm]),
=======
      Map("algRegression" -> classOf[Algorithm],
          "algGBTree"     -> classOf[AlgorithmGBTree],
          "ridgeRegression" -> classOf[RidgeRegressionAlgorithm]),
>>>>>>> 581b39ed71f2f0552c1b1e87830b54fccc87e57a
      classOf[Serving])
  }
}
