package dao

import common.CreateMySqlPool
import common.model.{HotWordInfo, MusicInfo}

import scala.collection.mutable.ArrayBuffer

object HotWordInfoDAO {



  def insertOrUpdate(hotWordInfo:HotWordInfo) {

    val sql = "update hot_word_info set hot_word_json = ? where hot_word_type=?"

    // 获取对象池单例对象
    val mySqlPool = CreateMySqlPool()
    // 从对象池中提取对象
    val client = mySqlPool.borrowObject()

    val  updateParams: Array[Any] = Array(hotWordInfo.hot_word_json, hotWordInfo.hot_word_type)
    // 执行批量插入操作
    client.executeUpdate(sql, updateParams)


    // 使用完成后将对象返回给对象池
    mySqlPool.returnObject(client)
  }

}
