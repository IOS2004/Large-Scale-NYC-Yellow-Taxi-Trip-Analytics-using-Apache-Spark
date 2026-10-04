package jobs

import consts.ArgumentsName.{INPUT_PATH, OUTPUT_PATH, TAXI_ZONE_PATH}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.Window
import utils.SchemaDefinitions
import utils.WriteUtils.saveDataframe
import utils.BenchmarkUtils

import java.nio.file.{Files, Paths}

object TLCAdvancedAnalyticsJob {
  def run(spark: SparkSession, args: Map[String, String]): Unit = {
    val inputPath: String = args.getOrElse(INPUT_PATH, "")
    val outputPath: String = args.getOrElse(OUTPUT_PATH, "")
    val taxiZonePath: String = args.getOrElse(TAXI_ZONE_PATH, "")

    if (!Files.exists(Paths.get(taxiZonePath)) || !Files.exists(Paths.get(inputPath))) {
      println(s"Input or Taxi Zone Paths do not exist.")
      sys.exit(1)
    }

    val tripDataSchema = SchemaDefinitions.GetTLCCleanedDataSchema()
    val taxiZoneSchema = SchemaDefinitions.GetTaxiZoneSchema()

    val tripDataDf = spark.read
      .schema(tripDataSchema)
      .format("delta")
      .load(inputPath)

    val taxiZoneDf = spark.read
      .schema(taxiZoneSchema)
      .option("header", "true")
      .csv(taxiZonePath)

    println("Starting Advanced Analytics Job...")

    // 3. Query Optimization Demonstration (Benchmarking Joins)
    // We run standard join and broadcast join to compare execution times.
    BenchmarkUtils.time("Standard Join execution time") {
      val standardJoinDf = tripDataDf.join(taxiZoneDf, col("pickup_location_id") === col("LocationID"))
      standardJoinDf.count()
    }

    BenchmarkUtils.time("Broadcast Hash Join execution time") {
      val broadcastJoinDf = tripDataDf.join(broadcast(taxiZoneDf), col("pickup_location_id") === col("LocationID"))
      broadcastJoinDf.count()
    }

    val joinedDf = tripDataDf.join(broadcast(taxiZoneDf), col("pickup_location_id") === col("LocationID"))
      .withColumnRenamed("Borough", "pickup_borough")
      .withColumnRenamed("Zone", "pickup_zone")

    // 1. Rolling 7-day average of taxi demand per borough (Window Functions)
    println("Calculating rolling 7-day average...")
    val dailyDemand = joinedDf
      .groupBy("pickup_borough", "year", "month", "day")
      .agg(count("*").as("daily_trips"))
      .withColumn("date", to_date(concat_ws("-", col("year"), col("month"), col("day"))))

    val windowSpec7Days = Window.partitionBy("pickup_borough").orderBy("date").rowsBetween(-6, 0)

    val rollingAvgDf = dailyDemand
      .withColumn("rolling_7_day_avg", avg("daily_trips").over(windowSpec7Days))

    saveDataframe(rollingAvgDf, outputPath, "rolling_7_day_avg")

    // 2. Rank Top 3 pickup zones by hour (Ranking)
    println("Calculating top 3 pickup zones by hour...")
    val hourlyZoneDemand = joinedDf
      .groupBy("hour", "pickup_zone")
      .agg(count("*").as("total_trips"))

    val windowSpecHourlyRank = Window.partitionBy("hour").orderBy(col("total_trips").desc)

    val rankedZonesDf = hourlyZoneDemand
      .withColumn("rank", rank().over(windowSpecHourlyRank))
      .filter(col("rank") <= 3)

    saveDataframe(rankedZonesDf, outputPath, "top_3_zones_by_hour")

    println("Advanced Analytics Job finished successfully!")
  }
}
