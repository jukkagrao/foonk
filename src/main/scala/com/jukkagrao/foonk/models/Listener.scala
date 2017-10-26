package com.jukkagrao.foonk.models

import akka.NotUsed
import akka.http.scaladsl.model.headers.`User-Agent`
import akka.http.scaladsl.model.{DateTime, RemoteAddress}
import akka.stream.scaladsl.Source
import akka.stream.{KillSwitch, KillSwitches, OverflowStrategy}
import akka.util.ByteString

case class Listener(streamPath: String,
                    id: Int,
                    ip: RemoteAddress,
                    userAgent: Option[`User-Agent`],
                    connected: DateTime,
                    killSwitch: KillSwitch,
                    stream: Source[ByteString, NotUsed]) extends StreamListener

//TODO: make possible to change Listener Stream
object Listener {
  def apply(path: String,
            ip: RemoteAddress,
            userAgent: Option[`User-Agent`],
            stream: Source[ByteString, NotUsed]): Listener = {
    val killSwitch = KillSwitches.shared(stream.hashCode.toString)
    //    val streamKillSwitch = KillSwitches.shared(path)
    val streamSource = stream.buffer(4, OverflowStrategy.dropNew)
      //      .via(streamKillSwitch.flow)
      .via(killSwitch.flow).async

    new Listener(path, streamSource.hashCode, ip, userAgent, DateTime.now, killSwitch, streamSource)
  }
}