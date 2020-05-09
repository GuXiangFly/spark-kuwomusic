package analyse

import analyse.SparkAnalyseRun.readFromTxtByLine
import common.{ConfigurationManager, Constants}
import org.apache.spark.sql.SparkSession

class AnalyseSinger {
  def musicCommentHotWord(spark: SparkSession): Unit = {
    spark.sql("select * from singer_info ")
  }
}
