package foonk.streams

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ContentType, MediaTypes}
import akka.stream.{KillSwitches, Materializer, SharedKillSwitch}
import akka.stream.scaladsl.{BroadcastHub, Keep, Source}
import akka.util.ByteString

import scala.concurrent.ExecutionContext


object SourceMediaStream {

  def apply(path: String,
            source: Source[ByteString, Any],
            contentType: ContentType = MediaTypes.`audio/mpeg`,
            public: Boolean = true,
            description: Option[String] = None,
            genre: Option[String] = None,
            bitrate: Option[String] = None,
            url: Option[String] = None)(implicit sys: ActorSystem, mat: Materializer, ev: ExecutionContext): MediaStream = {

    val killSwitch = KillSwitches.shared(path)
    val src = source.via(killSwitch.flow).toMat(BroadcastHub.sink[ByteString])(Keep.right).run()
    SourceMediaStream(path, src, contentType, public, description, genre, bitrate, url, killSwitch)
  }

  private case class SourceMediaStream(path: String,
                                       source: Source[ByteString, Any],
                                       contentType: ContentType = MediaTypes.`audio/mpeg`,
                                       public: Boolean = true,
                                       description: Option[String] = None,
                                       genre: Option[String] = None,
                                       bitrate: Option[String] = None,
                                       url: Option[String] = None,
                                       killSwitch: SharedKillSwitch)
                                      (implicit sys: ActorSystem, mat: Materializer, ev: ExecutionContext)
    extends MediaStream {

    println(s"Source $path created")

    def stream: Source[ByteString, NotUsed] = switcher.stream
  }

}