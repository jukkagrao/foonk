package com.jukkagrao.foonk.http

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.jukkagrao.foonk.db.{ListenerDb, StreamDb}
import com.jukkagrao.foonk.http.directives.Directives._
import com.jukkagrao.foonk.models.Listener
import com.jukkagrao.foonk.utils.Logger

object ListenersHandler extends Logger {
  def route(implicit as: ActorSystem): Route =
    (get & streamPath & extractClientIP & userAgent) { (sPath, ip, ua) =>
      StreamDb.get(sPath) match {
        case Some(strm) =>
          import as.dispatcher

          val listener = Listener(sPath, ip, ua, strm.stream)
          ListenerDb.update(listener.id, listener)

          log.info(s"Listener ${listener.id} connected, IP: ${listener.ip.toOption.getOrElse("unknown")}," +
            s" ${listener.userAgent.getOrElse("unknown")}")

          respondWithIcyHeaders(strm) {
            complete(HttpResponse(entity = HttpEntity(strm.contentType,
              listener.stream.watchTermination() {
                              (mat, futDone) =>
                                futDone.onComplete ( _ => ListenerDb.remove(listener.id))
                                log.info(s"Listener ${listener.id} disconnected")
                                mat
                            }
            )))
          }
        case None => complete(StatusCodes.NotFound)
      }
    }

}
