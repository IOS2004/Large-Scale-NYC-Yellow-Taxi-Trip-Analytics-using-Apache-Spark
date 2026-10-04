package utils

import org.apache.spark.sql.SparkSession
import com.typesafe.config.ConfigFactory

object SparkUtils {
  /**
   * Creates and returns a SparkSession with the provided jobName and configuration.
   *
   * @param jobName The name of the Spark job.
   * @return The created SparkSession instance.
   */
  def getSparkSession(jobName: String): SparkSession = {
    val config = ConfigFactory.load()
    val master = config.getString("app.spark.master")

    val spark = SparkSession.builder()
      .appName(jobName)
      .master(master)
      .config("spark.sql.extensions", config.getString("app.spark.sql.extensions"))
      .config("spark.sql.catalog.spark_catalog", config.getString("app.spark.sql.catalog.spark_catalog"))
      .getOrCreate()

    spark
  }
}
