package com.jukkagrao.foonk.listeners


import akka.NotUsed
import akka.http.scaladsl.model.{DateTime, RemoteAddress}
import akka.stream.scaladsl.Source
import akka.stream.{KillSwitch, KillSwitches, OverflowStrategy}
import akka.util.ByteString

case class Listener(streamPath: String,
                    id: Int,
                    ip: RemoteAddress,
                    connected: DateTime,
                    killSwitch: KillSwitch,
                    stream: Source[ByteString, NotUsed]) extends StreamListener

object Listener {
  def apply(path: String, ip: RemoteAddress, stream: Source[ByteString, NotUsed]): Listener = {
    val killSwitch = KillSwitches.shared(stream.hashCode.toString)
//    val streamKillSwitch = KillSwitches.shared(path)
    val streamSource = stream.buffer(8, OverflowStrategy.dropNew)
//      .via(streamKillSwitch.flow)
      .via(killSwitch.flow).async

    new Listener(path, streamSource.hashCode, ip, DateTime.now, killSwitch, streamSource)
  }
}