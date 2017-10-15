package com.jukkagrao.foonk.models


import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.stream.{KillSwitches, Materializer, SharedKillSwitch}
import akka.stream.scaladsl.{BroadcastHub, Keep, Source}
import akka.util.ByteString
import com.jukkagrao.foonk.http.headers._

import scala.concurrent.ExecutionContext


object RelayMediaStream {

  def apply(path: String,
            response: HttpResponse)
           (implicit sys: ActorSystem,
            mat: Materializer,
            ev: ExecutionContext): MediaStream = {

    val contentType = response.entity.contentType

    val killSwitch = KillSwitches.shared(path)

    val src = response.entity.withoutSizeLimit.dataBytes
      .via(killSwitch.flow).toMat(BroadcastHub.sink[ByteString](bufferSize = 2))(Keep.right).run

    def findHeader(lowerCaseName: String) = response.headers.find(h => h.is(lowerCaseName))

    val pub = findHeader(`Icy-Public`.lowercaseName).forall(_.value.toBoolean)
    val name = findHeader(`Icy-Name`.lowercaseName).map(_.value)
    val description = findHeader(`Icy-Description`.lowercaseName).map(_.value)
    val genre = findHeader(`Icy-Genre`.lowercaseName).map(_.value)
    val bitrate = findHeader(`Icy-Bitrate`.lowercaseName).map(_.value)
    val url = findHeader(`Icy-Url`.lowercaseName).map(_.value)
    val audioInfo = findHeader(`Icy-Audio-Info`.lowercaseName).map(_.value)
    val connected = DateTime.now

    RelayMediaStream(path, src, contentType, pub, name, description, genre, bitrate, url, audioInfo, connected, killSwitch)
  }

  private case class RelayMediaStream(path: String,
                                      source: Source[ByteString, Any],
                                      contentType: ContentType = MediaTypes.`audio/mpeg`,
                                      public: Boolean = true,
                                      name: Option[String] = None,
                                      description: Option[String] = None,
                                      genre: Option[String] = None,
                                      bitrate: Option[String] = None,
                                      url: Option[String] = None,
                                      audioInfo: Option[String] = None,
                                      connected: DateTime,
                                      killSwitch: SharedKillSwitch)
                                     (implicit sys: ActorSystem, mat: Materializer, ev: ExecutionContext)
    extends MediaStream {

    sys.log.info(s"Relay $path created")

    def stream: Source[ByteString, NotUsed] = switcher.stream
  }

}
