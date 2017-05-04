import AssemblyKeys._

assemblySettings

name := "predictionio-load-forecasting"

libraryDependencies ++= Seq(
  "org.apache.predictionio"    %% "apache-predictionio-core" % "0.10.0-incubating" % "provided",
<<<<<<< HEAD
  "org.apache.spark"  %% "spark-core"   % "1.3.0"           % "provided",
  "org.apache.spark"  %% "spark-mllib"  % "1.3.0"           % "provided"
=======
  "org.apache.spark"  %% "spark-core"   % "1.5.2"           % "provided",
  "org.apache.spark"  %% "spark-mllib"  % "1.5.2"           % "provided"
>>>>>>> 581b39ed71f2f0552c1b1e87830b54fccc87e57a
)
