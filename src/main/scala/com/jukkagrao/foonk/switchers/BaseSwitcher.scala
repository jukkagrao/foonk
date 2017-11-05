package com.jukkagrao.foonk.switchers

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{BroadcastHub, Keep, MergeHub, Sink, Source}
import akka.stream.{KillSwitch, KillSwitches, Materializer}
import akka.util.ByteString
import com.jukkagrao.foonk.models.MediaStream
import com.jukkagrao.foonk.utils.Logger

case class Tag(tag: String) extends AnyVal

class BaseSwitcher(mStream: MediaStream, source: Source[ByteString, Any])
                           (implicit as: ActorSystem, m: Materializer)
  extends Logger {

  private[switchers] val defaultTag = Tag(mStream.mount)
  private[switchers] var current = defaultTag


  private[switchers] var killSwitch: KillSwitch = KillSwitches.shared(s"$current-${this.getClass.getName}")

  val (sink, stream) = MergeHub.source[(Tag, ByteString)](perProducerBufferSize = 1)
    .filter(_._1 == current).map(_._2)
    .toMat(BroadcastHub.sink[ByteString](bufferSize = 1))(Keep.both).run

  stream.runWith(Sink.ignore)

  protected[switchers] def setDefaultStream(source: Source[ByteString, Any]): NotUsed = {
    // connect Source to MergeHub with recover for case of Source Termination
    source
      .recoverWithRetries(-1, { case _: Throwable => Source.empty })
      .map(s => (defaultTag, s))
      .runWith(sink)
  }

  setDefaultStream(source)

  private[switchers] def switchToDefaultStream(): Unit = {
    current = defaultTag
  }

}
