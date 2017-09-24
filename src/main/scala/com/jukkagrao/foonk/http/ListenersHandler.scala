package com.jukkagrao.foonk.http

import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.stream.OverflowStrategy
import com.jukkagrao.foonk.db.StreamDb
import com.jukkagrao.foonk.http.headers._

object ListenersHandler {
  val route: Route =
    get {
      extractRequest { request =>
        val path = request.uri.path.toString()
        StreamDb.get(path) match {
          case Some(strm) =>
            complete(HttpResponse(entity =
              HttpEntity(strm.contentType,
                strm.stream.buffer(8, OverflowStrategy.dropHead)
              )
            ).withHeaders(
              `Icy-Name`(strm.name.getOrElse("")),
              `Icy-Genre`(strm.genre.getOrElse("")),
              `Icy-Description`(strm.description.getOrElse(""))
            ))
          case None => complete(StatusCodes.NotFound)
        }
      }
    }

}
