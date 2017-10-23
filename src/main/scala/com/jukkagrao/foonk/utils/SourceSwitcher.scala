package com.jukkagrao.foonk.utils

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{BroadcastHub, Keep, MergeHub, Sink, Source}
import akka.util.ByteString
import com.jukkagrao.foonk.models.MediaStream

import scala.concurrent.ExecutionContext


class SourceSwitcher(mStream: MediaStream)
                    (implicit sys: ActorSystem,
                     mat: Materializer,
                     executionService: ExecutionContext) {

  private var current = mStream.mount

  val (sink: Sink[(String, ByteString), NotUsed], stream: Source[ByteString, NotUsed]) =
    MergeHub.source[(String, ByteString)].filter(_._1 == current)
      .map(_._2).toMat(BroadcastHub.sink[ByteString](bufferSize = 2))(Keep.both)
      .run

  stream.runWith(Sink.ignore)

  // add default stream
  mStream.source.map(s => (mStream.mount, s)).runWith(sink)

  def switchBack(): Unit = current = mStream.mount

  def switchTo(thatSrc: MediaStream): Unit = {
    thatSrc.source.map(s => (thatSrc.mount, s)).runWith(sink)
    current = thatSrc.mount
  }

  def currentStream: String = current

}

object SourceSwitcher {

  def apply(src: MediaStream)
           (implicit sys: ActorSystem,
            mat: Materializer,
            executionService: ExecutionContext) = new SourceSwitcher(src)

}
