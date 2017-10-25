package com.jukkagrao.foonk.http

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.pattern.{CircuitBreaker, after}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.jukkagrao.foonk.db.StreamDb
import com.jukkagrao.foonk.models.RelayMediaStream
import com.jukkagrao.foonk.utils.{Logger, RelaySource}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Success


class RelayClient(source: RelaySource)
                 (implicit as: ActorSystem,
                  m: ActorMaterializer) extends Logger {

  import as.dispatcher

  private[this] val breaker =
    new CircuitBreaker(
      as.scheduler,
      maxFailures = 5,
      callTimeout = 10.seconds,
      resetTimeout = 15.seconds).onOpen {
      log.warning(s"CircuitBreaker was opened while requesting to ${source.uri}")
    }

  private[this] def request: Future[HttpResponse] =
    breaker.withCircuitBreaker(Http().singleRequest(HttpRequest(uri = source.uri)))

  def setupStream(): Unit = setupPermanent(source.retryTimeout)

  private[this] def setupPermanent(timeout: FiniteDuration): Unit =
    request.onComplete {
      case Success(response@HttpResponse(StatusCodes.OK, _, _, _)) =>
        val mediaStream = RelayMediaStream(source.mount, response)
        StreamDb.update(source.mount, mediaStream)
        val done: Future[Done] = mediaStream.source.runWith(Sink.ignore)
        done.onComplete { _ =>
          StreamDb.remove(source.mount)
          setupPermanent(timeout)
        }
      case _ => after(timeout, as.scheduler)(Future.successful(setupPermanent(timeout)))
    }
}
