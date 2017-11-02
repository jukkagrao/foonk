package com.jukkagrao.foonk.http

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.jukkagrao.foonk.collections.{ClientCollection, StreamCollection}
import com.jukkagrao.foonk.http.directives.Directives._
import com.jukkagrao.foonk.models.Client
import com.jukkagrao.foonk.utils.Logger

object ClientsHandler extends Logger {
  def route(implicit as: ActorSystem): Route =
    (get & streamPath & extractClientIP & userAgent) { (sPath, ip, ua) =>
      StreamCollection.get(sPath) match {
        case Some(strm) =>
          import as.dispatcher

          val client = Client(sPath, ip, ua, strm.stream)
          ClientCollection.update(client.id, client)

          log.info(s"Client ${client.id} connected to /$sPath, IP: ${client.ip.toOption.getOrElse("unknown")}," +
            s" ${client.userAgent.getOrElse("unknown")}.")

          respondWithIcyHeaders(strm) {
            complete(HttpResponse(entity = HttpEntity(strm.contentType,
              client.stream.watchTermination() {
                (mat, futDone) =>
                  futDone.onComplete { _ =>
                    log.info(s"Client ${client.id} disconnected.")
                    ClientCollection.remove(client.id)
                  }
                  mat
              }
            )))
          }
        case None => complete(StatusCodes.NotFound)
      }
    }

}
