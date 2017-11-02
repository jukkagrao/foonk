package com.jukkagrao.foonk.collections

import com.jukkagrao.foonk.models.StreamClient


object ClientCollection extends BaseCollection[Int, StreamClient] {

  def getByPath(path: String): List[(Int, StreamClient)] = all.filter(_._2.streamPath == path)

  def countByPath(path: String): Int = getByPath(path).size

}
