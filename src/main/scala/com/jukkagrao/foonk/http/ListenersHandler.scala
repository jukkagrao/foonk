package com.jukkagrao.foonk.http

import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import com.jukkagrao.foonk.db.StreamDb

object ListenersHandler {
  val route: Route =
    get {
      extractRequest { request =>
        val path = request.uri.path.toString()
        complete {
          StreamDb.get(path) match {
            case Some(stream) => HttpResponse(entity = HttpEntity(stream.contentType, stream.stream))
            case None => StatusCodes.NotFound
          }
        }
      }
    }
}
