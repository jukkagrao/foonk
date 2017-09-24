package com.jukkagrao.foonk.http

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.Sink
import akka.http.scaladsl.server.Route
import akka.stream.{Materializer, OverflowStrategy}
import com.jukkagrao.foonk.db.StreamDb
import com.jukkagrao.foonk.http.auth.SourceAuthenticator
import com.jukkagrao.foonk.http.methods.SourceMethod
import com.jukkagrao.foonk.http.headers._
import com.jukkagrao.foonk.streams.SourceMediaStream

import scala.concurrent.{ExecutionContext, Future}

class IncomingSourceHandler(implicit sys: ActorSystem, mat: Materializer, ex: ExecutionContext) {
  val route: Route =
    (put | method(SourceMethod.method)) {
      authenticateBasic(realm = "foonk source", SourceAuthenticator.authenticator) { _ =>
        extractRequest { request =>
          val path = request.uri.path.toString
          val data = request
            .entity
            .withoutSizeLimit()
            .dataBytes
            .buffer(8, OverflowStrategy.backpressure)

          val mediaStream = SourceMediaStream(
            path,
            data,
            request.entity.contentType,
            public = true,
            request.headers.find(h => h.lowercaseName == `Ice-Name`.lowercaseName).map(_.value),
            request.headers.find(h => h.lowercaseName == `Ice-Description`.lowercaseName).map(_.value),
            request.headers.find(h => h.lowercaseName == `Ice-Genre`.lowercaseName).map(_.value)
          )
          StreamDb.update(path, mediaStream)

          val done: Future[Done] = mediaStream.source.runWith(Sink.ignore)

          onComplete(done) { _ =>
            StreamDb.remove(path)
            complete(StatusCodes.NoContent)
          }
        }
      }
    }
}

object IncomingSourceHandler {
  def apply()(implicit sys: ActorSystem, mat: Materializer, ex: ExecutionContext): Route =
    new IncomingSourceHandler().route
}
