package com.jukkagrao.foonk.switchers

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.stream._
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.util.ByteString
import com.jukkagrao.foonk.collections.{FallbackSwitcherCollection, StreamCollection}
import com.jukkagrao.foonk.models.MediaStream

import scala.concurrent.duration._

class FallbackSwitcher(mStream: MediaStream, source: Source[ByteString, Any])
                      (implicit as: ActorSystem, m: Materializer)
  extends BaseSwitcher(mStream, source) {

  import as.dispatcher

  private[this] val fbTag = Tag("fallback")
  private[this] var fbMount = ""

  protected def fallback: String = fbMount


  protected def setFallback(fallbackStream: MediaStream): Unit = {
    if (fallbackStream.contentType == mStream.contentType) {

      switchToDefaultStream()

      killSwitch.shutdown()

      killSwitch = fallbackStream.stream
        .recoverWithRetries(-1, { case _: Throwable => Source.empty })
        .map(s => (fbTag, s))
        .viaMat(KillSwitches.single)(Keep.right)
        .toMat(sink)(Keep.left).run

      stream.idleTimeout(1.second).watchTermination() {
        (mat, futDone) =>
          futDone.onComplete(_ => switchToFallback())
          mat
      }.runWith(Sink.ignore)

      fbMount = fallbackStream.mount
    }
  }

  def switchToFallback(): Unit = {
    if (current != fbTag && fallback.nonEmpty) {
      current = fbTag
      log.warning(s"Mount /${mStream.mount} was switched to fallback (/$fallback).")
    }
  }

  def addFallback(fallbackStream: MediaStream): HttpResponse = {
    if (mStream.mount == fallbackStream.mount)
      HttpResponse(StatusCodes.Conflict, entity = "Mount and fallback are the same.")
    else if (fallbackStream.mount == fbMount)
      HttpResponse(StatusCodes.AlreadyReported, entity = "Mount already uses that fallback.")
    else if (mStream.contentType != fallbackStream.contentType)
      HttpResponse(StatusCodes.ExpectationFailed, entity = "Mounts have different Content-Types.")
    else {
      setFallback(fallbackStream)
      log.info(s"Fallback to /${fallbackStream.mount} for mount /${mStream.mount} was set up.")
      HttpResponse(StatusCodes.Created, entity = "Done.")
    }
  }

  def removeFallback(): HttpResponse = {
    if (fallback.nonEmpty) {
      fbMount = ""

      killSwitch.shutdown()

      log.info(s"Fallback for mount /${mStream.mount} was removed.")
      HttpResponse(StatusCodes.Created, entity = "Done.")
    } else
      HttpResponse(StatusCodes.NotFound)
  }

}


object FallbackSwitcher {

  def apply(src: MediaStream)
           (implicit as: ActorSystem,
            mat: Materializer): FallbackSwitcher = {
    val mnt = src.mount
    FallbackSwitcherCollection.get(mnt) match {
      case Some(switcher) =>
        switcher.setDefaultStream(src.source)
        StreamCollection.get(switcher.fallback).foreach(fs => switcher.setFallback(fs))
        switcher
      case None =>
        val switcher = new FallbackSwitcher(src, src.source)
        FallbackSwitcherCollection.update(mnt, switcher)
        switcher
    }
  }
}
