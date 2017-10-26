package com.jukkagrao.foonk.models


import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.stream.scaladsl.{BroadcastHub, Keep, Source}
import akka.stream.{KillSwitches, Materializer, SharedKillSwitch}
import akka.util.ByteString
import com.jukkagrao.foonk.http.headers._
import com.jukkagrao.foonk.utils.HttpUtils._
import com.jukkagrao.foonk.utils.{Logger, SourceSwitcher}


object RelayMediaStream extends Logger {

  def apply(path: String,
            response: HttpResponse)
           (implicit as: ActorSystem,
            mat: Materializer): MediaStream = {

    val contentType = response.entity.contentType

    val killSwitch = KillSwitches.shared(path)

    val src = response.entity
      .withoutSizeLimit
      .dataBytes
      .via(killSwitch.flow)
      .toMat(BroadcastHub.sink[ByteString])(Keep.right)
      .run

    implicit val resp: HttpResponse = response

    val pub = findHeader(`Icy-Public`.lowercaseName).forall(_.value == "1")
    val name = findHeader(`Icy-Name`.lowercaseName).map(h => urlDecode(h.value))
    val description = findHeader(`Icy-Description`.lowercaseName).map(h => urlDecode(h.value))
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
                                     (implicit as: ActorSystem,
                                      mat: Materializer)
    extends MediaStream {

    log.info(s"Relay $mount created")

    val switcher = SourceSwitcher(this)

    val stream: Source[ByteString, NotUsed] = switcher.stream
  }

}
