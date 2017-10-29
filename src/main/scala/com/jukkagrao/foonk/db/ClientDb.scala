package com.jukkagrao.foonk.db

import com.jukkagrao.foonk.models.StreamClient


object ClientDb extends BaseDb[Int, StreamClient] {

  def getByPath(path: String): List[(Int, StreamClient)] = all.filter(_._2.streamPath == path)

  def countByPath(path: String): Int = getByPath(path).size

}
