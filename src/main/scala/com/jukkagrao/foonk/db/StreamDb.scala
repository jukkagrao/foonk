package com.jukkagrao.foonk.db

import com.jukkagrao.foonk.streams.MediaStream

import scala.collection.parallel.mutable
import scala.collection.parallel.mutable.ParHashMap


object StreamDb {

  private val streams: ParHashMap[String, MediaStream] = new mutable.ParHashMap

  def update(key: String, value: MediaStream): Option[MediaStream] = streams.put(key, value)

  def remove(key: String): Option[MediaStream] = {
    streams.get(key) match {
      case Some(stream) =>
        stream.kill()
        streams.remove(key)
      case None => None
    }
  }

  def get(key: String): Option[MediaStream] = streams.get(key)

  def all: List[(String, MediaStream)] = streams.toList

}
