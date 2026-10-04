package jobs

import consts.{ArgumentsName, Jobs}
import utils._
import com.typesafe.config.ConfigFactory

object TLCAnalysisApp {
  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()
    val argsMap: Map[String, String] = args.flatMap(StringUtils.convertArgsToMap).toMap
    val jobName: String = argsMap.getOrElse(ArgumentsName.JOB_NAME, "")

    if(jobName.isBlank || jobName.isEmpty) {
      print("Usage: Need to provide jobName parameter.")
      sys.exit(1)
    }

    val spark = SparkUtils.getSparkSession(jobName)

    val jobArgs = Map(
      ArgumentsName.INPUT_PATH -> config.getString("app.paths.input_data"),
      ArgumentsName.OUTPUT_PATH -> config.getString("app.paths.output_data"),
      ArgumentsName.TAXI_ZONE_PATH -> config.getString("app.paths.taxi_zones")
    ) ++ argsMap

    jobName match {
      case Jobs.SCHEMA_FIXER_JOB            => TLCSchemaFixerJob.run(spark, jobArgs)
      case Jobs.DATA_CLEANER_AND_PROCESSOR  => TLCDataCleanerJob.run(spark, jobArgs)
      case Jobs.DATA_ANALYSIS               => TLCDataAnalysisJob.run(spark, jobArgs)
      case Jobs.ADVANCED_ANALYSIS           => TLCAdvancedAnalyticsJob.run(spark, jobArgs)
      case Jobs.MACHINE_LEARNING            => TLCMachineLearningJob.run(spark, jobArgs)
      case _ =>
    }

    spark.stop()
  }
}
