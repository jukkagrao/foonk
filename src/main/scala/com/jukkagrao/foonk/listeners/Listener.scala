package com.jukkagrao.foonk.listeners


import akka.NotUsed
import akka.http.scaladsl.model.{DateTime, RemoteAddress}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Source
import akka.util.ByteString

case class Listener(streamPath: String,
                    id: Int,
                    ip: RemoteAddress,
                    connected: DateTime,
                    stream: Source[ByteString, NotUsed]) extends StreamListener

object Listener {
  def apply(path: String, ip: RemoteAddress, stream: Source[ByteString, NotUsed]): Listener = {
    val strm = stream.buffer(8, OverflowStrategy.dropNew).async
    new Listener(path, strm.hashCode(), ip, DateTime.now, strm)
  }
}