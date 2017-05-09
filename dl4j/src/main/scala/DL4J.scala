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
import scala.io._
import org.nd4s.Implicits._

object DL4J extends App {

  // Read in training data
  val f = "features.csv"
  val l = "labels.csv"
  val featuresArray = Source.fromFile(f).getLines().map(_.split(",").map(_.trim.toDouble).toArray).toArray
  val labelsArray = Source.fromFile(l).getLines().map(_.trim.toDouble).toArray

  println(featuresArray.size)
  println(labelsArray.size)
  println(labelsArray(0))

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

  val labelsINDArray = labelsArray.asNDArray(labelsArray.length,1)
  println(labelsINDArray.size(0) + " " + labelsINDArray.size(1))

  val featuresINDArray = Nd4j.create(featuresArray)
  println(featuresINDArray.size(0) + " " + featuresINDArray.size(1))

  var dataset = new DataSet(featuresINDArray, labelsINDArray)
  var inputINDArray = Nd4j.create(featuresArray)

  model.fit(dataset)

  val testArray = Array(0.01,1.25,1.3,-0.2,0.0,0.0,0.0,0.0,1.0);
  val test = Nd4j.create(testArray)
  val out = model.output(test, false)
  System.out.println(out)




}
