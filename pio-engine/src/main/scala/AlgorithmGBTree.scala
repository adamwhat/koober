package edu.cs5152.predictionio.demandforecasting
import grizzled.slf4j.Logger
import org.apache.predictionio.controller.{CustomQuerySerializer, P2LAlgorithm}
import org.apache.spark.SparkContext
import org.apache.spark.mllib.clustering.KMeansModel
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LinearRegressionModel
import org.apache.spark.mllib.tree.GradientBoostedTrees
import org.apache.spark.mllib.tree.configuration.BoostingStrategy
import org.apache.spark.mllib.tree.model.GradientBoostedTreesModel
import org.joda.time.DateTime

/**
  * Created by YitingWang on 3/2/17.
  */
class AlgorithmGBTree extends P2LAlgorithm[PreparedData, ModelGBTree, Query, PredictedResult] with MyQuerySerializer{

  override def train(sc: SparkContext, preparedData: PreparedData): ModelGBTree ={
    val boostingStrategy = BoostingStrategy.defaultParams("Regression")
    boostingStrategy.setNumIterations(10)
    boostingStrategy.getTreeStrategy().setMaxDepth(10)
    //  Empty categoricalFeaturesInfo indicates all features are continuous.
    //boostingStrategy.getTreeStrategy().setCategoricalFeaturesInfo(Map[Int, Int]())

    val gradientBoostedTreeModel = GradientBoostedTrees.train(preparedData.data, boostingStrategy)
    new ModelGBTree(gradientBoostedTreeModel, Preparator.locationClusterModel.get)
  }

  override def predict(model: ModelGBTree, query: Query): PredictedResult = {
    val label : Double = model.predict(query)
    new PredictedResult(label)
  }
}

class ModelGBTree(mod: GradientBoostedTreesModel, locationClusterModel: KMeansModel) extends Serializable { // will not be DateTime after changes
// to Preparator
@transient lazy val logger = Logger[this.type]

  def predict(query: Query): Double = {
    val locationClusterLabel = locationClusterModel.predict(Vectors.dense(query.lat, query.lng))
    val features = Preparator.toFeaturesVector(DateTime.parse(query.eventTime), query.lat, query.lng, locationClusterLabel)
    mod.predict(features)
  }
}
