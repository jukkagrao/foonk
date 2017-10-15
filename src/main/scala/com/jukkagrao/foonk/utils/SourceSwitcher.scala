package com.jukkagrao.foonk.utils

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{BroadcastHub, Keep, MergeHub, Sink, Source}
import akka.util.ByteString
import com.jukkagrao.foonk.models.MediaStream

import scala.concurrent.ExecutionContext


class SourceSwitcher(src: MediaStream)
                    (implicit sys: ActorSystem,
                     mat: Materializer,
                     executionService: ExecutionContext) {

  private var current = src.mount

  val (sink: Sink[(String, ByteString), NotUsed], stream: Source[ByteString, NotUsed]) =
    MergeHub.source[(String, ByteString)].filter(_._1 == current)
//      .via(src.killSwitch.flow)
      .map(_._2).toMat(BroadcastHub.sink[ByteString](bufferSize = 8))(Keep.both)
      .run()

  stream.runWith(Sink.ignore)

  // add default stream
  src.source.map(s => (src.mount, s)).runWith(sink)

  def switchBack(): Unit = current = src.mount

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
