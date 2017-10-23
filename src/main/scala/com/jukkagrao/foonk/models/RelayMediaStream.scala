package com.jukkagrao.foonk.models


import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.stream.scaladsl.{BroadcastHub, Keep, Source}
import akka.stream.{KillSwitches, Materializer, SharedKillSwitch}
import akka.util.ByteString
import com.jukkagrao.foonk.http.headers._
import com.jukkagrao.foonk.utils.Logger

import scala.concurrent.ExecutionContext


object RelayMediaStream extends Logger {

  def apply(path: String,
            response: HttpResponse)
           (implicit sys: ActorSystem,
            mat: Materializer,
            ev: ExecutionContext): MediaStream = {

    val contentType = response.entity.contentType

    val killSwitch = KillSwitches.shared(path)

    val src = response.entity
      .withoutSizeLimit
      .dataBytes
      .via(killSwitch.flow)
      .toMat(BroadcastHub.sink[ByteString])(Keep.right)
      .run

    def findHeader(lowerCaseName: String) = response.headers.find(h => h.is(lowerCaseName))

    val pub = findHeader(`Icy-Public`.lowercaseName).forall(_.value.toBoolean)
    val name = findHeader(`Icy-Name`.lowercaseName).map(_.value)
    val description = findHeader(`Icy-Description`.lowercaseName).map(_.value)
    val genre = findHeader(`Icy-Genre`.lowercaseName).map(_.value)
    val bitrate = findHeader(`Icy-Br`.lowercaseName).map(_.value)
    val url = findHeader(`Icy-Url`.lowercaseName).map(_.value)
    val audioInfo = findHeader(`Icy-Audio-Info`.lowercaseName).map(_.value)
    val streamInfo = StreamInfo(name, description, genre, bitrate, url, audioInfo, pub)
    val connected = DateTime.now

    RelayMediaStream(path, src, contentType, streamInfo, connected, killSwitch)
  }

  private case class RelayMediaStream(mount: String,
                                      source: Source[ByteString, Any],
                                      contentType: ContentType = MediaTypes.`audio/mpeg`,
                                      info: StreamInfo,
                                      connected: DateTime,
                                      killSwitch: SharedKillSwitch)
                                     (implicit sys: ActorSystem, mat: Materializer, ev: ExecutionContext)
    extends MediaStream {

    log.info(s"Relay $mount created")

    def stream: Source[ByteString, NotUsed] = switcher.stream
  }

}
