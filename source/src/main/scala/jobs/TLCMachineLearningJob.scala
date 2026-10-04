package jobs

import consts.ArgumentsName.{INPUT_PATH, OUTPUT_PATH}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.classification.RandomForestClassifier
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator
import org.apache.spark.sql.types.DoubleType
import utils.SchemaDefinitions

import java.nio.file.{Files, Paths}

object TLCMachineLearningJob {
  def run(spark: SparkSession, args: Map[String, String]): Unit = {
    val inputPath: String = args.getOrElse(INPUT_PATH, "")
    val outputPath: String = args.getOrElse(OUTPUT_PATH, "")

    if (!Files.exists(Paths.get(inputPath))) {
      println(s"Input path $inputPath does not exist.")
      sys.exit(1)
    }

    val tripDataSchema = SchemaDefinitions.GetTLCCleanedDataSchema()

    val tripDataDf = spark.read
      .schema(tripDataSchema)
      .format("delta")
      .load(inputPath)

    println("Starting Machine Learning Pipeline...")

    // 2. Feature Engineering
    // Predict high tippers (tip > 20% of fare_amount)
    // We only consider trips with a fare_amount > 0 to avoid division by zero.
    val filteredDf = tripDataDf.filter(col("fare_amount") > 0)
    
    val featureDf = filteredDf
      .withColumn("tip_percentage", col("tip_amount") / col("fare_amount"))
      .withColumn("is_high_tipper", when(col("tip_percentage") > 0.20, 1.0).otherwise(0.0))
      .withColumn("passenger_count_d", col("passenger_count").cast(DoubleType))
      .withColumn("hour_d", col("hour").cast(DoubleType))

    // Handle nulls in features
    val cleanFeatureDf = featureDf
      .na.fill(0.0, Seq("trip_distance", "trip_duration_in_minutes", "passenger_count_d", "hour_d"))

    val assembler = new VectorAssembler()
      .setInputCols(Array("trip_distance", "trip_duration_in_minutes", "passenger_count_d", "hour_d"))
      .setOutputCol("features")

    val assembledData = assembler.transform(cleanFeatureDf).select("features", "is_high_tipper")

    // 3. Model Training & Evaluation
    val Array(trainingData, testData) = assembledData.randomSplit(Array(0.8, 0.2), seed = 1234L)

    println("Training Random Forest Classifier...")
    val rf = new RandomForestClassifier()
      .setLabelCol("is_high_tipper")
      .setFeaturesCol("features")
      .setNumTrees(10) // Small number of trees for faster execution during the project
      .setMaxDepth(5)

    val model = rf.fit(trainingData)

    println("Evaluating the model...")
    val predictions = model.transform(testData)

    val evaluator = new BinaryClassificationEvaluator()
      .setLabelCol("is_high_tipper")
      .setRawPredictionCol("rawPrediction")
      .setMetricName("areaUnderROC")

    val accuracy = evaluator.evaluate(predictions)
    println(s"⏱️ Model Area Under ROC: $accuracy")

    // Save the model
    val modelPath = outputPath + "/models/rf_tip_model"
    println(s"Saving model to $modelPath")
    model.write.overwrite().save(modelPath)

    println("Machine Learning Job finished successfully!")
  }
}
