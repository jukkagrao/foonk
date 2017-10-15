package com.jukkagrao.foonk.db

import com.jukkagrao.foonk.models.StreamListener


object ListenerDb extends BaseDb[Int, StreamListener] {

  def getByPath(path: String): List[(Int, StreamListener)] = all.filter(_._2.streamPath == path)

  def countByPath(path: String): Int = getByPath(path).size

}
