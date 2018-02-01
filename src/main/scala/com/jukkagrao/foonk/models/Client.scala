package com.jukkagrao.foonk.models

import akka.NotUsed
import akka.http.scaladsl.model.headers.`User-Agent`
import akka.http.scaladsl.model.{DateTime, RemoteAddress}
import akka.stream.scaladsl.Source
import akka.stream.{KillSwitch, KillSwitches, OverflowStrategy}
import akka.util.ByteString

case class Client(streamPath: String,
                  id: Int,
                  ip: RemoteAddress,
                  userAgent: Option[`User-Agent`],
                  connected: DateTime,
                  killSwitch: KillSwitch,
                  stream: Source[ByteString, NotUsed]) extends StreamClient

//TODO: make possible to change Client Stream
object Client {
  def apply(path: String,
            ip: RemoteAddress,
            userAgent: Option[`User-Agent`],
            stream: Source[ByteString, NotUsed]): Client = {
    val killSwitch = KillSwitches.shared(stream.hashCode.toString)
    val streamSource = stream.buffer(4, OverflowStrategy.dropNew)
      .via(killSwitch.flow).async

    new Client(path, streamSource.hashCode, ip, userAgent, DateTime.now, killSwitch, streamSource)
  }
}