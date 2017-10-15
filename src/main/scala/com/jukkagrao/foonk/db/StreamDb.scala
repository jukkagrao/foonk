package com.jukkagrao.foonk.db

import com.jukkagrao.foonk.models.MediaStream

import scala.collection.parallel.mutable
import scala.collection.parallel.mutable.ParTrieMap


object StreamDb {

  private val streams: ParTrieMap[String, MediaStream] = new mutable.ParTrieMap[String, MediaStream]()

  def update(key: String, value: MediaStream): Option[MediaStream] = streams.put(key, value)

  def remove(key: String): Option[MediaStream] = {
    streams.get(key) flatMap { stream =>
      stream.kill()
      streams.remove(key)
    }
  }

  def get(key: String): Option[MediaStream] = streams.get(key)

  def all: List[(String, MediaStream)] = streams.toList

}
