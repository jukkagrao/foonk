package foonk.http

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.Sink
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import foonk.http.auth.SourceAuthenticator
import foonk.http.methods.SourceMethod
import foonk.streams.{SourceMediaStream, StreamDb}

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

          val mediaStream = SourceMediaStream(path, data)
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
