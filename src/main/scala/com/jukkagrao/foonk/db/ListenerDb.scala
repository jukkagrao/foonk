package com.jukkagrao.foonk.db

import com.jukkagrao.foonk.listeners.StreamListener

import scala.collection.parallel.mutable
import scala.collection.parallel.mutable.ParHashMap


object ListenerDb {

  private val listeners: ParHashMap[Int, StreamListener] = new mutable.ParHashMap

  def update(id: Int, value: StreamListener): Option[StreamListener] = listeners.put(id, value)

  def remove(id: Int): Option[StreamListener] = {
      listeners.remove(id)
  }

  def get(id: Int): Option[StreamListener] = listeners.get(id)

  def all: List[(Int, StreamListener)] = listeners.toList

  def getByPath(path: String): List[(Int, StreamListener)] = all.filter(_._2.streamPath == path)

  def countByPath(path: String): Int = getByPath(path).size

}
