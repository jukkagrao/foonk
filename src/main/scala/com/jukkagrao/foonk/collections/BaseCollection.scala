package com.jukkagrao.foonk.collections

import scala.collection.parallel.mutable.ParTrieMap


trait BaseCollection[A, B] {

  private[collections] val entries: ParTrieMap[A, B] = new ParTrieMap[A, B]

  def update(key: A, value: B): Option[B] = entries.put(key, value)

  def get(key: A): Option[B] = entries.get(key)

  def exist(key: A): Boolean = entries.keys.exists(_ == key)

  def all: List[(A, B)] = entries.toList

  def remove(key: A): Option[B] = {
    entries.remove(key)
  }

}
