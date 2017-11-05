package com.jukkagrao.foonk.switchers

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.{KillSwitches, Materializer}
import akka.util.ByteString
import com.jukkagrao.foonk.models.MediaStream


class SourceSwitcher(mStream: MediaStream, source: Source[ByteString, Any])
                    (implicit as: ActorSystem, m: Materializer)
  extends BaseSwitcher(mStream, source) {


  def switchToInit(): HttpResponse = {
    if (defaultTag == current)
      HttpResponse(StatusCodes.AlreadyReported, entity = "Mount already uses that source.")
    else {
      // close previous switched stream
      killSwitch.shutdown()

      current = defaultTag

      log.info(s"Mount ${mStream.mount} was switched to initial source.")
      HttpResponse(StatusCodes.Created, entity = "Done.")
    }
  }

  def switchTo(thatSrc: MediaStream): HttpResponse = {
    if (Tag(thatSrc.mount) == current)
      HttpResponse(StatusCodes.AlreadyReported, entity = "Mount already uses that source.")
    else if (Tag(thatSrc.mount) == defaultTag)
      HttpResponse(StatusCodes.Conflict, entity = "Switching on source itself not allow.")
    else if (mStream.contentType != thatSrc.contentType)
      HttpResponse(StatusCodes.UnsupportedMediaType, entity = "Mounts have different Content-Types.")
    else {
      // close previous switched stream
      killSwitch.shutdown()

      killSwitch = thatSrc.source
        .viaMat(KillSwitches.single)(Keep.right)
        .map(s => (Tag(thatSrc.mount), s))
        .toMat(sink)(Keep.left).run

      switchToDefaultStream()

      log.info(s"Mount /${mStream.mount} was switched to /${current.tag}.")
      HttpResponse(StatusCodes.Created, entity = s"Done.")
    }
  }

}


object SourceSwitcher {
  def apply(src: MediaStream)
           (implicit as: ActorSystem,
            mat: Materializer): SourceSwitcher = {
    val source = src.fallback.stream
    new SourceSwitcher(src, source)
  }
}
