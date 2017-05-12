package edu.cs5152.predictionio.demandforecasting

import grizzled.slf4j.Logger
import org.apache.predictionio.controller.{CustomQuerySerializer, P2LAlgorithm, Params}
import org.apache.spark.SparkContext
import org.apache.spark.mllib.clustering.KMeansModel
import org.apache.spark.mllib.feature.StandardScalerModel
import org.apache.spark.mllib.linalg.Vectors
import org.joda.time.DateTime

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction.MSE;


case class NeuralNetAlgorithmParams(
  seed:               Int     = 12345,
  iterations:         Int     = 500,
  learningRate:       Double  = 0.1,
  numInputs:          Int     = 212,
  listenerFreq:       Int     = 2
) extends Params

class NeuralNetAlgorithm(val ap: NeuralNetAlgorithmParams)
  extends P2LAlgorithm[PreparedData, NeuralNetModel, Query, PredictedResult] with NeuralNetQuerySerializer {

  @transient lazy val logger = Logger[this.type]

  def train(sc: SparkContext, preparedData: PreparedData): NeuralNetModel = {

    val conf : MultiLayerConfiguration
    = new NeuralNetConfiguration.Builder()
      .seed(ap.seed)
      .iterations(ap.iterations)
      .learningRate(ap.learningRate)
      .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
      .weightInit(WeightInit.XAVIER)
      .updater(Updater.NESTEROVS).momentum(0.9)
      .list()
      .layer(0, new DenseLayer.Builder().nIn(212).nOut(256)
        .activation(Activation.RELU).build())
      .layer(1, new DenseLayer.Builder().nIn(256).nOut(100)
        .activation(Activation.RELU).build())
      .layer(2, new OutputLayer.Builder(MSE)
        .activation(Activation.IDENTITY)
        .nIn(100).nOut(1).build())
      .backprop(true).pretrain(false).build()

    val model : MultiLayerNetwork = new MultiLayerNetwork(conf)
    model.init()
    model.setListeners(new ScoreIterationListener(ap.listenerFreq))

    model.fit(preparedData.dataSet)

    new NeuralNetModel(model, Preparator.locationClusterModel.get, Preparator.standardScalerModel.get)
  }

  def predict(model: NeuralNetModel, query: Query): PredictedResult = {
    val label : Double = model.predict(query)
    new PredictedResult(label, Map("alg" -> label))
  }
}

class NeuralNetModel(mod: MultiLayerNetwork, locationClusterModel: KMeansModel, standardScalerModel: StandardScalerModel) extends Serializable {
  @transient lazy val logger = Logger[this.type]

  def predict(query: Query) : Double = {
    println("1")
    val normalizedFeatureVector = standardScalerModel.transform(Preparator.toFeaturesVector(DateTime.parse(query.eventTime),
      query.temperature, query.clear, query.fog, query.rain, query.snow, query.hail, query.thunder, query.tornado))
    println("2")
    val locationClusterLabel = locationClusterModel.predict(Vectors.dense(query.lat, query.lng))
    println("3")
    val features = Preparator.combineFeatureVectors(normalizedFeatureVector, locationClusterLabel)
    println("4")
    println (features.toArray)
    val out = mod.output(Nd4j.create(features.toArray))
    println(out)
    println(out.max(0))
    println(out.max(1))

    10.0
  }
}

trait NeuralNetQuerySerializer extends CustomQuerySerializer {
  @transient override lazy val querySerializer = org.json4s.DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all
}

