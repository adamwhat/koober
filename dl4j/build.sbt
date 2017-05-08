name := "dl4j"

libraryDependencies ++= Seq(
  "org.apache.predictionio"    %% "apache-predictionio-core" % "0.10.0-incubating" % "provided",
  "org.apache.spark"  %% "spark-core"   % "1.3.0"           % "provided",
  "org.apache.spark"  %% "spark-mllib"  % "1.3.0"           % "provided",
  "org.deeplearning4j" % "deeplearning4j-core" % "0.8.0",
  "org.nd4j" % "nd4j-native-platform" % "0.8.0",
  "org.nd4j" %% "nd4s" % "0.8.0"
)
