package com.jukkagrao.foonk.utils

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{BroadcastHub, Keep, MergeHub, Sink, Source}
import akka.util.ByteString
import com.jukkagrao.foonk.models.MediaStream


class SourceSwitcher(mStream: MediaStream)
                    (implicit as: ActorSystem,
                     m: Materializer) extends Logger {

  private var current = mStream.mount

  val (sink: Sink[(String, ByteString), NotUsed], stream: Source[ByteString, NotUsed]) =
    MergeHub.source[(String, ByteString)].filter(_._1 == current)
      .map(_._2).toMat(BroadcastHub.sink[ByteString](bufferSize = 2))(Keep.both).run

  stream.runWith(Sink.ignore)

  // add default stream
  mStream.source.map(s => (mStream.mount, s)).runWith(sink)

  def switchBack(): Unit = {
    current = mStream.mount
    log.info(s"Mount ${mStream.mount} was switched to initial source")
  }

  def switchTo(thatSrc: MediaStream): Unit = {
    thatSrc.source.map(s => (thatSrc.mount, s)).runWith(sink)
    current = thatSrc.mount
    log.info(s"Mount ${mStream.mount} was switched to ${thatSrc.mount}")
  }

  def currentStream: String = current

}

object SourceSwitcher {

  def apply(src: MediaStream)
           (implicit as: ActorSystem,
            mat: Materializer) = new SourceSwitcher(src)

}
