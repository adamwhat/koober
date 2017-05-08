package edu.cs5152.predictionio.demandforecasting

import grizzled.slf4j.Logger
import org.apache.predictionio.controller.{CustomQuerySerializer, P2LAlgorithm, Params}
import org.apache.spark.SparkContext
import org.apache.spark.mllib.clustering.KMeansModel
import org.apache.spark.mllib.feature.StandardScalerModel
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.{LinearRegressionModel, LinearRegressionWithSGD}
import org.joda.time.DateTime

//import org.deeplearning4j.datasets.iterator.DataSetIterator;
//import org.deeplearning4j.datasets.iterator.impl.IrisDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
//import org.deeplearning4j.nn.conf.layers.Layer
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.params.DefaultParamInitializer;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.optimize.api.IterationListener;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction.MSE;

case class AlgorithmParams(
  seed:               Int     = 12345,
  iterations:         Int     = 20,
  learningRate:       Double  = 0.1,
  layers:             Int     = 2,
  numInputs:          Int     = 10,
  numOutputs:         Int     = 1,
  listenerFreq:       Int     = 2
) extends Params

class Algorithm(val ap: AlgorithmParams)
  extends P2LAlgorithm[PreparedData, Model, Query, PredictedResult] with MyQuerySerializer {

  @transient lazy val logger = Logger[this.type]

  def train(sc: SparkContext, preparedData: PreparedData): Model = {

    val conf : MultiLayerConfiguration
    = new NeuralNetConfiguration.Builder()
      .seed(12345)
      .iterations(50)
      .learningRate(0.1)
      .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
      .weightInit(WeightInit.XAVIER)
      .updater(Updater.NESTEROVS).momentum(0.9)
      .list()
      .layer(0, new DenseLayer.Builder().nIn(9).nOut(256)
        .activation(Activation.RELU).build())
      .layer(1, new DenseLayer.Builder().nIn(256).nOut(100)
        .activation(Activation.RELU).build())
      .layer(2, new OutputLayer.Builder(MSE)
        .activation(Activation.IDENTITY)
        .nIn(100).nOut(1).build())
      .backprop(true).pretrain(false).build()

    val model : MultiLayerNetwork = new MultiLayerNetwork(conf)
    model.init()
    model.setListeners(new ScoreIterationListener(1))

    model.fit(preparedData.dataSet)

    new Model(model, Preparator.locationClusterModel.get, Preparator.standardScalerModel.get)
  }

  def predict(model: Model, query: Query): PredictedResult = {
    val label : Double = model.predict(query)
    new PredictedResult(label)
  }
}

class Model(mod: MultiLayerNetwork, locationClusterModel: KMeansModel, standardScalerModel: StandardScalerModel) extends Serializable { 
  @transient lazy val logger = Logger[this.type]

  def predict(query: Query) : Double = {
    val normalizedTimeFeatureVector = standardScalerModel.transform(Preparator.toFeaturesVector(DateTime.parse(query.eventTime), query.lat, query.lng))
    val locationClusterLabel = locationClusterModel.predict(Vectors.dense(query.lat, query.lng))
    val features = Preparator.toFeaturesVector(normalizedTimeFeatureVector, locationClusterLabel)
    println (features.toArray)
    mod.predict(Nd4j.create(features.toArray))(0).toDouble
  }
}

trait MyQuerySerializer extends CustomQuerySerializer {
  @transient override lazy val querySerializer = org.json4s.DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all
}

