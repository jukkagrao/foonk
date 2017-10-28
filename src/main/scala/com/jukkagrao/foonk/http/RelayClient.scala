package com.jukkagrao.foonk.http

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.pattern.{CircuitBreaker, after}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{RestartSource, Sink, Source}
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

  def setupStream(): Unit = setupPermanentSource(source.retryTimeout)

  private[this] def setupPermanentSource(timeout: FiniteDuration): Unit = {
    request.onComplete {
      case Success(response@HttpResponse(StatusCodes.OK, _, entity, _)) =>
        entity.dataBytes.runWith(Sink.cancelled)

        val restartSource = RestartSource.withBackoff(
          minBackoff = 1.second,
          maxBackoff = 60.seconds,
          randomFactor = 0.2 // adds 20% "noise" to vary the intervals slightly
        )(streamSource)

        val mediaStream = RelayMediaStream(source.mount, response, restartSource)
        StreamDb.update(source.mount, mediaStream)
        val done: Future[Done] = mediaStream.source.runWith(Sink.ignore)
        done.onComplete { _ =>
          log.info(s"Source /${source.mount} disconnected.")
          StreamDb.remove(source.mount)
          setupPermanentSource(timeout)
        }

      case _ => after(timeout, as.scheduler)(Future.successful(setupPermanentSource(timeout)))
    }
  }

  private[this] def streamSource = () => {
    log.info(s"Connecting source /${source.mount}...")
    Source.fromFutureSource(request.map {
      case HttpResponse(StatusCodes.OK, _, ent, _) =>
        log.info(s"Source /${source.mount} connected.")
        ent.withoutSizeLimit.dataBytes
    })
  }
}
