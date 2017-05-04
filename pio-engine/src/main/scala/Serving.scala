package edu.cs5152.predictionio.demandforecasting

import org.apache.predictionio.controller.LServing

class Serving extends LServing[Query, PredictedResult] {

  override def serve(query: Query,
                     predictedResults: Seq[PredictedResult]): PredictedResult = {
<<<<<<< HEAD
    predictedResults.head
=======
    println(predictedResults.length)
    println(predictedResults.head.demand)
    println(predictedResults.last.demand)
    var sumResult:Double = 0.0
    predictedResults.foreach(sumResult += _.demand)

    //val sumResult: Double = predictedResults.foldLeft(0.0){( acc: Double, pred: PredictedResult) => acc + pred.demand}
    println(sumResult)
    val meanResult: Double = sumResult / predictedResults.length
    new PredictedResult(meanResult)
>>>>>>> 581b39ed71f2f0552c1b1e87830b54fccc87e57a
  }
}
