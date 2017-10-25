package com.jukkagrao.foonk.http

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.Sink
import akka.stream.{Materializer, OverflowStrategy}
import com.jukkagrao.foonk.db.StreamDb
import com.jukkagrao.foonk.http.directives.Directives._
import com.jukkagrao.foonk.models.SourceMediaStream

import scala.concurrent.Future

class IncomingSourceHandler(implicit as: ActorSystem, mat: Materializer) {

  val route: Route =
    iceSource {
      streamPath { sPath =>
        extractIceHeaders { (iName, iDesc, iGenre, iBitrate, iAudioInfo, iUrl, iPublic) =>
          extractRequestEntity { entity =>
            val data =
              entity
                .withoutSizeLimit()
                .dataBytes
                .buffer(8, OverflowStrategy.backpressure)

            val mediaStream = SourceMediaStream(
              sPath,
              data,
              entity.contentType,
              iPublic,
              iName,
              iDesc,
              iGenre,
              iBitrate,
              iAudioInfo,
              iUrl
            )

            StreamDb.update(sPath, mediaStream)

            val done: Future[Done] = mediaStream.source.runWith(Sink.ignore)

            onComplete(done) { _ =>
              StreamDb.remove(sPath)
              complete(StatusCodes.NoContent)
            }
          }
        }
      }
    }

}

object IncomingSourceHandler {
  def apply()(implicit as: ActorSystem, mat: Materializer): Route = new IncomingSourceHandler().route
}
