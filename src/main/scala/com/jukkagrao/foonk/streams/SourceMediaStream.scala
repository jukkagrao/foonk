package com.jukkagrao.foonk.streams

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ContentType, DateTime, MediaTypes}
import akka.stream.{KillSwitches, Materializer, SharedKillSwitch}
import akka.stream.scaladsl.{BroadcastHub, Keep, Source}
import akka.util.ByteString

import scala.concurrent.ExecutionContext
import scala.util.Try


object SourceMediaStream {

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
    val src = source.via(killSwitch.flow).toMat(BroadcastHub.sink[ByteString](bufferSize = 8))(Keep.right).run
    val pub = public.forall(p => Try(p.toBoolean).getOrElse(true))
    val connected = DateTime.now

    SourceMediaStream(path, src, contentType, pub, name, description, genre, bitrate, url, audioInfo, connected, killSwitch)
  }

  private case class SourceMediaStream(path: String,
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

    sys.log.info(s"Source $path created")

    def stream: Source[ByteString, NotUsed] = switcher.stream
  }

}
