package com.jukkagrao.foonk.models

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ContentType, DateTime, MediaTypes}
import akka.stream.scaladsl.{BroadcastHub, Keep, Source}
import akka.stream.{KillSwitches, Materializer, SharedKillSwitch}
import akka.util.ByteString
import com.jukkagrao.foonk.utils.Logger

import scala.concurrent.ExecutionContext
import scala.util.Try


object SourceMediaStream extends Logger {

  def apply(path: String,
            source: Source[ByteString, Any],
            contentType: ContentType = MediaTypes.`audio/mpeg`,
            public: Option[String] = None,
            name: Option[String] = None,
            description: Option[String] = None,
            genre: Option[String] = None,
            bitrate: Option[String] = None,
            audioInfo: Option[String] = None,
            url: Option[String] = None)
           (implicit sys: ActorSystem,
            mat: Materializer,
            ev: ExecutionContext): MediaStream = {

    val killSwitch = KillSwitches.shared(path)
    val src = source.via(killSwitch.flow).toMat(BroadcastHub.sink[ByteString](bufferSize = 2))(Keep.right).run
    val pub = public.forall(p => Try(p.toBoolean).getOrElse(true))
    val streamInfo = StreamInfo(name, description, genre, bitrate, url, audioInfo, pub)
    val connected = DateTime.now

    SourceMediaStream(path, src, contentType, streamInfo, connected, killSwitch)
  }

  private case class SourceMediaStream(mount: String,
                                       source: Source[ByteString, Any],
                                       contentType: ContentType = MediaTypes.`audio/mpeg`,
                                       info: StreamInfo,
                                       connected: DateTime,
                                       killSwitch: SharedKillSwitch)
                                      (implicit sys: ActorSystem, mat: Materializer, ev: ExecutionContext)
    extends MediaStream {

    log.info(s"Source $mount created")

    def stream: Source[ByteString, NotUsed] = switcher.stream
  }

}
