package com.jukkagrao.foonk.http

import akka.Done
import akka.actor.ActorSystem
import akka.dispatch.MessageDispatcher
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.pattern.{CircuitBreaker, after}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.jukkagrao.foonk.db.{OnDemandRelayDb, StreamDb}
import com.jukkagrao.foonk.models.{RelayMediaStream, StreamInfo}
import com.jukkagrao.foonk.utils.{Logger, RelaySource}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Success


class RelayClient(source: RelaySource)
                 (implicit ec: ExecutionContext,
                  as: ActorSystem,
                  m: ActorMaterializer) extends Logger {

  private[this] val breaker =
    new CircuitBreaker(
      as.scheduler,
      maxFailures = 5,
      callTimeout = 10.seconds,
      resetTimeout = source.connectionTimeout * 15).onOpen {
      log.warning(s"CircuitBreaker was opened while requesting to ${source.uri}")
    }

  private[this] def request: Future[HttpResponse] =
    breaker.withCircuitBreaker(Http().singleRequest(HttpRequest(uri = source.uri)))

  def setupStream(): Unit =
    if (source.onDemand) setupOnDemand() else setupPermanent(source.connectionTimeout)

  def requestOnDemand(): Unit = {
    implicit val blockingDispatcher: MessageDispatcher =
      as.dispatchers.lookup("blocking-dispatcher")

    Await.result(request, 5.second) match {
      case response@HttpResponse(StatusCodes.OK, _, _, _) =>
        val mediaStream = RelayMediaStream(source.mount, response)
        StreamDb.update(source.mount, mediaStream)
        OnDemandRelayDb.update(source.mount, mediaStream.info)
      case _ =>
        val info = StreamInfo()
        OnDemandRelayDb.update(source.mount, info)
    }
  }

  private[this] def setupPermanent(timeout: FiniteDuration): Unit =
    after(timeout, as.scheduler)(request).onComplete {
      case Success(response@HttpResponse(StatusCodes.OK, _, _, _)) =>
        val mediaStream = RelayMediaStream(source.mount, response)
        StreamDb.update(source.mount, mediaStream)
        val done: Future[Done] = mediaStream.source.runWith(Sink.ignore)
        done.onComplete { _ =>
//          StreamDb.remove(source.mount)
          setupPermanent(timeout)
        }
      case _ => setupPermanent(timeout)
    }

  private[this] def setupOnDemand() = {
    val info = StreamInfo()
    OnDemandRelayDb.update(source.mount, info)
  }

}
