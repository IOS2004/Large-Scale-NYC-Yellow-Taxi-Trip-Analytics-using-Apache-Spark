package utils

import org.apache.spark.sql.{DataFrame, SaveMode}

object WriteUtils {
   def saveDataframe(dataFrame: DataFrame, outputPath: String, fileName: String = ""): Unit = {
    dataFrame.write
      .format("delta")
      .mode(SaveMode.Overwrite)
      .save(outputPath + "/" + fileName)
  }
}
