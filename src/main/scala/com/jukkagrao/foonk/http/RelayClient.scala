package com.jukkagrao.foonk.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.jukkagrao.foonk.db.StreamDb
import com.jukkagrao.foonk.models.RelayMediaStream

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}


class RelayClient(uri: String, streamPath: String)
                 (implicit ec: ExecutionContext, as: ActorSystem, m: ActorMaterializer) {


  private[this] def request(timeout: FiniteDuration = 1.second): Future[HttpResponse] =
      Http().singleRequest(HttpRequest(uri = uri))

//  private[this] def request(attempt: Int, timeout: FiniteDuration = 1.second): Future[HttpResponse] =
//    breaker.withCircuitBreaker(
//      Http().singleRequest(HttpRequest(uri = uri)).recoverWith {
//        case e: Throwable => if (attempt > 0)
//          pattern.after(timeout, as.scheduler)(request(attempt - 1))
//        else Future.failed(e)
//      }.map {
//        case resp@HttpResponse(StatusCodes.OK, _, _, _) => resp
//      }
//    )

  def setupStream(onDemand: Boolean = false): Unit = request().onComplete {
    case Success(response@HttpResponse(StatusCodes.OK, _, _, _)) =>
      val mediaStream = RelayMediaStream(streamPath, response)
      StreamDb.update(streamPath, mediaStream)

      mediaStream.source.runWith(Sink.ignore)
    case Failure(e) => e.printStackTrace()
    case _ => setupStream(onDemand)
  }

  def setupOnDemand(): Unit = request().onComplete {
    case Success(response@HttpResponse(StatusCodes.OK, _, _, _)) =>
      val mediaStream = RelayMediaStream(streamPath, response)
      StreamDb.update(streamPath, mediaStream)
    case Failure(e) => e.printStackTrace()
    case _ => setupOnDemand()
  }


}
