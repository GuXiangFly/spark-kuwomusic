package analyse
import common.{ConfigurationManager, Constants}
import common.model.MusicInfo
import org.apache.spark.SparkConf
import com.huaban.analysis.jieba.{JiebaSegmenter, SegToken}
import com.huaban.analysis.jieba.JiebaSegmenter.SegMode
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._


object SparkSQL08_MySQL {
  def main(args: Array[String]): Unit = {

    val config : SparkConf = new SparkConf().setMaster("local[1]").setAppName("SparkSQL_HIVE_Demo")

    val spark: SparkSession = SparkSession
      .builder()
      .appName("Spark Hive Example2")
      .config(config)
      .getOrCreate()

    val jdbcDF = spark.read
      .format("jdbc")
      .option("url", ConfigurationManager.config.getString(Constants.JDBC_URL))
      .option("dbtable", "music_info")
      .option("user", ConfigurationManager.config.getString(Constants.JDBC_USER))
      .option("password", ConfigurationManager.config.getString(Constants.JDBC_PASSWORD))
      .load()

    import spark.implicits._
    var musicInfoListRdd = jdbcDF.as[MusicInfo].rdd
    musicInfoListRdd.foreach(
      item =>{
        println(item.music_lyric)
      }
    )
    

    spark.stop()
  }


  /**
   * 分词方法
   * @param df（DataFrame，需要处理数据源）
   * @param columnName（DataFrame数据源需要处理的列）
   * @return
   */
  def jieba_seg(df:DataFrame, columnName:String) : DataFrame = {
    val segmenter = new JiebaSegmenter()
    // 通过spark上下文把jieba分词对象发送到广播中（其他节点通过广播使用）
    val seg = df.sparkSession.sparkContext.broadcast(segmenter)
    // 定义udf函数
    val jieba_udf = udf{ (sentence:String) =>
      // 通过spark广播获取jieba分词对象(分布式)
      val segV = seg.value
      // jieba分词对象处理一个句子
      segV.process(sentence.toString, SegMode.INDEX)
        .toArray().map(_.asInstanceOf[SegToken].word)
        .filter(_.length>1).mkString("/")
    }
    /**
     * 调用udf分词函数
     */
    df.withColumn("seg", jieba_udf(col(columnName)))
  }
}
