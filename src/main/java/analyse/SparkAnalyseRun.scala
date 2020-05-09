package analyse

import java.io.InputStream
import java.util

import common.{ConfigurationManager, Constants}
import common.model.{HotWordBean, HotWordInfo, MusicInfo}
import org.apache.spark.SparkConf
import com.huaban.analysis.jieba.{JiebaSegmenter, SegToken}
import com.huaban.analysis.jieba.JiebaSegmenter.SegMode
import net.sf.json.JSONObject
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._
import com.alibaba.fastjson.JSON

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import com.google.gson.Gson
import com.google.gson.JsonObject
import dao.HotWordInfoDAO


object SparkAnalyseRun {
  def main(args: Array[String]): Unit = {

    val config: SparkConf = new SparkConf().setMaster("local[1]").setAppName("SparkSQL_HIVE_Demo")

    val spark: SparkSession = SparkSession
      .builder()
      .appName("Spark Hive Example2")
      .config(config)
      .getOrCreate()

    musicLyricHotWord(spark)
    musicCommentHotWord(spark)

    spark.stop()
  }



   def musicLyricHotWord(spark: SparkSession): Unit = {
    val strings: Array[String] = readFromTxtByLine("/stopwords/baidu_stopwords.txt")
    strings.foreach(print)

    val jdbcDF = spark.read
      .format("jdbc")
      .option("url", ConfigurationManager.config.getString(Constants.JDBC_URL))
      .option("dbtable", "music_info")
      .option("user", ConfigurationManager.config.getString(Constants.JDBC_USER))
      .option("password", ConfigurationManager.config.getString(Constants.JDBC_PASSWORD))
      .load()


    import spark.implicits._
    var musicInfoListRdd: RDD[MusicInfo] = jdbcDF.as[MusicInfo].rdd


    val wordSetRdd: RDD[String] = musicInfoListRdd.flatMap(item => {
      println(item.music_lyric)
      val segmenter = new JiebaSegmenter()
      var wordSet = new mutable.HashSet[String]()
      val result = segmenter.process(item.music_lyric, SegMode.INDEX)
        .toArray().map(_.asInstanceOf[SegToken].word)
        .filter(_.length > 1).mkString(" ")
      result.split(" ").foreach(keyword => {
        if (!strings.contains(keyword)) {
          wordSet.+=(keyword)
        }
      })
      wordSet
    })

    val wordToOne: RDD[(String, Int)] = wordSetRdd.map((_, 1))

    val wordToSum: RDD[(String, Int)] = wordToOne.reduceByKey(_ + _)

    val tuples: Array[(String, Int)] = wordToSum.sortBy(_._2, false).take(50)

    val resultList = new util.ArrayList[HotWordBean]
    tuples.foreach(item =>{
      val hotWord = new HotWordBean(item._1, item._2)
      resultList.add(hotWord)
    })

    val gson = new Gson()
    val jsonResult: String = gson.toJson(resultList)

    println(jsonResult)

    val hotWordInfo = new HotWordInfo(1,"歌词热词",jsonResult)
    HotWordInfoDAO.insertOrUpdate(hotWordInfo)
  }




   def musicCommentHotWord(spark: SparkSession): Unit = {
    val strings: Array[String] = readFromTxtByLine("/stopwords/baidu_stopwords.txt")
    strings.foreach(print)

    val jdbcDF = spark.read
      .format("jdbc")
      .option("url", ConfigurationManager.config.getString(Constants.JDBC_URL))
      .option("dbtable", "music_info")
      .option("user", ConfigurationManager.config.getString(Constants.JDBC_USER))
      .option("password", ConfigurationManager.config.getString(Constants.JDBC_PASSWORD))
      .load()


    import spark.implicits._
    var musicInfoListRdd: RDD[MusicInfo] = jdbcDF.as[MusicInfo].rdd


    val wordSetRdd: RDD[String] = musicInfoListRdd.flatMap(item => {
      val segmenter = new JiebaSegmenter()
      var wordSet = new mutable.HashSet[String]()
      val result = segmenter.process(item.music_comment, SegMode.INDEX)
        .toArray().map(_.asInstanceOf[SegToken].word)
        .filter(_.length > 1).mkString(" ")
      result.split(" ").foreach(keyword => {
        if (!strings.contains(keyword)) {
          wordSet.+=(keyword)
        }
      })
      wordSet
    })

    val wordToOne: RDD[(String, Int)] = wordSetRdd.map((_, 1))

    val wordToSum: RDD[(String, Int)] = wordToOne.reduceByKey(_ + _)

    val tuples: Array[(String, Int)] = wordToSum.sortBy(_._2, false).take(50)

    val resultList = new util.ArrayList[HotWordBean]
    tuples.foreach(item =>{
      val hotWord = new HotWordBean(item._1, item._2)
      resultList.add(hotWord)
    })

    val gson = new Gson()
    val jsonResult: String = gson.toJson(resultList)

    println(jsonResult)

    val hotWordInfo = new HotWordInfo(1,"评论热词",jsonResult)
    HotWordInfoDAO.insertOrUpdate(hotWordInfo)
  }



  def readFromTxtByLine(filePath:String) = {

    val stream : InputStream = getClass.getResourceAsStream(filePath)
    val lines = scala.io.Source.fromInputStream( stream ).getLines.toArray
    lines
  }

}
