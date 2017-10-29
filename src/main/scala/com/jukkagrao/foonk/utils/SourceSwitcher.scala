package com.jukkagrao.foonk.utils

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.stream.scaladsl.{BroadcastHub, Keep, MergeHub, Sink, Source}
import akka.stream.{KillSwitch, KillSwitches, Materializer}
import akka.util.ByteString
import com.jukkagrao.foonk.models.MediaStream

import scala.util.Try


class SourceSwitcher(mStream: MediaStream)
                    (implicit as: ActorSystem,
                     m: Materializer) extends Logger {

  private var current = mStream.mount
  private val withFallback = s"${mStream.mount}-with-fallback"
  private var killSwitch: KillSwitch = KillSwitches.shared(s"${mStream.mount}-switch")
  private var fallbackKillSwitch: KillSwitch = KillSwitches.shared(s"${mStream.mount}-fallback-switch")

  val (sink: Sink[(String, ByteString), NotUsed], stream: Source[ByteString, NotUsed]) =
    MergeHub.source[(String, ByteString)]
      .recoverWithRetries(-1, { case _: Exception ⇒ Source.empty })
      .filter(_._1 == current)
      .map(_._2).toMat(BroadcastHub.sink[ByteString](bufferSize = 2))(Keep.both).run

  stream.runWith(Sink.ignore)


  // add default stream
  mStream.source.map(s => (mStream.mount, s)).runWith(sink)

  def currentStream: String = current

  def switchBack(): HttpResponse = {
    if (mStream.mount == current)
      HttpResponse(StatusCodes.AlreadyReported, entity = "Mount already uses that source.")
    else {
      // close previous switched stream
      Try(killSwitch.shutdown())

      current = mStream.mount
      log.info(s"Mount ${mStream.mount} was switched to initial source.")
      HttpResponse(StatusCodes.Created)
    }
  }

  def switchTo(thatSrc: MediaStream): HttpResponse = {
    if (thatSrc.mount == current)
      HttpResponse(StatusCodes.AlreadyReported, entity = "Mount already uses that source.")
    else if (mStream.contentType != thatSrc.contentType)
      HttpResponse(StatusCodes.ExpectationFailed, entity = "Mounts have different Content-Types.")
    else {
      // close previous switched stream
      Try(killSwitch.shutdown())

      killSwitch = thatSrc.source
        .viaMat(KillSwitches.single)(Keep.right)
        .map(s => (thatSrc.mount, s))
        .toMat(sink)(Keep.left).run

      current = thatSrc.mount
      log.info(s"Mount ${mStream.mount} was switched to $current.")

      HttpResponse(StatusCodes.Created)
    }
  }

  def setFallback(fallbackSrc: MediaStream): HttpResponse = {
    if (mStream.mount == fallbackSrc.mount)
      HttpResponse(StatusCodes.Conflict, entity = "Mount and fallback are the same.")
    else if (mStream.contentType != fallbackSrc.contentType)
      HttpResponse(StatusCodes.ExpectationFailed, entity = "Mounts have different Content-Types.")
    else {
      // Switch to fallback in case of error
      mStream.source.recoverWith({ case _ => fallbackSrc.stream })
        .map(s => (withFallback, s))
        .runWith(sink)

      current = withFallback

      log.info(s"Fallback to ${fallbackSrc.mount} for mount ${mStream.mount} was set up.")

      HttpResponse(StatusCodes.Created)
    }
  }

  def removeFallback(): HttpResponse = {
    if (current == withFallback) {
      current = mStream.mount

      Try(fallbackKillSwitch.shutdown())

      log.info(s"Fallback for mount ${mStream.mount} was removed.")
      HttpResponse(StatusCodes.Created)
    } else
      HttpResponse(StatusCodes.NotFound)
  }

}

object SourceSwitcher {

  def apply(src: MediaStream)
           (implicit as: ActorSystem,
            mat: Materializer) = new SourceSwitcher(src)

}
