import AssemblyKeys._

assemblySettings

name := "predictionio-load-forecasting"

libraryDependencies ++= Seq(
  "org.apache.predictionio"    %% "apache-predictionio-core" % "0.10.0-incubating" % "provided",
  "org.apache.spark"  %% "spark-core"   % "1.3.0"           % "provided",
  "org.apache.spark"  %% "spark-mllib"  % "1.3.0"           % "provided",
  "org.deeplearning4j" % "deeplearning4j-core" % "0.7.2"    % "provided",
  "org.nd4j" % "nd4j-jblas" % "0.4-rc3.6"                   % "provided"
)
