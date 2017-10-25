package com.jukkagrao.foonk.http

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.jukkagrao.foonk.db.{ListenerDb, StreamDb}
import com.jukkagrao.foonk.http.directives.Directives._
import com.jukkagrao.foonk.models.Listener

object ListenersHandler {
  def route(implicit as: ActorSystem): Route =
    (get & streamPath & extractClientIP) { (sPath, ip) =>
      StreamDb.get(sPath) match {
        case Some(strm) =>
          import as.dispatcher

          val listener = Listener(sPath, ip, strm.stream)
          ListenerDb.update(listener.id, listener)

          respondWithIcyHeaders(strm) {
            complete(HttpResponse(entity = HttpEntity(strm.contentType,
              listener.stream.watchTermination() {
                              (mat, futDone) =>
                                futDone.onComplete ( _ => ListenerDb.remove(listener.id))
                                mat
                            }
            )))
          }
        case None => complete(StatusCodes.NotFound)
      }
    }

}
