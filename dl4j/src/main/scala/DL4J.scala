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

object DL4J extends App {

  // Read in training data
  val f = "features.csv"
  val l = "labels.csv"
  val featuresArray = Source.fromFile(f).getLines().map(_.split(",").map(_.trim.toDouble)).toArray
  val labelsArray = Source.fromFile(l).getLines().map(_.trim.toDouble).toArray

  val conf : MultiLayerConfiguration
  = new NeuralNetConfiguration.Builder()
    .seed(12345)
    .iterations(20)
    .learningRate(0.1)
    .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
    .weightInit(WeightInit.XAVIER)
    .updater(Updater.NESTEROVS).momentum(0.9)
    .list()
    .layer(0, new DenseLayer.Builder().nIn(9).nOut(20)
      .activation(Activation.TANH).build())
    .layer(1, new RnnOutputLayer.Builder(MSE)
      .activation(Activation.IDENTITY)
      .nIn(20).nOut(10000).build())
    .backprop(true).pretrain(false).build()
  conf.setPretrain(true)
  val model : MultiLayerNetwork = new MultiLayerNetwork(conf)
  model.init()
  model.setListeners(new ScoreIterationListener(2))

  var dataset = new DataSet(Nd4j.create(featuresArray), Nd4j.create(labelsArray))
  var inputINDArray = Nd4j.create(featuresArray)

  model.fit(inputINDArray, Nd4j.create(labelsArray))

//  MultiLayerNetworkExternalErrors.main(args)

}