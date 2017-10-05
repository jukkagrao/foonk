package com.jukkagrao.foonk.http.api

import javax.ws.rs.Path

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.jukkagrao.foonk.db.{ListenerDb, StreamDb}
import com.jukkagrao.foonk.http.api.serializers.{ListenerSerializer, MediaStreamInfoSerializer, MediaStreamSerializer, MediaStreamsSerializer}
import com.jukkagrao.foonk.http.directives.Directives._
import io.swagger.annotations.{Api, ApiOperation, ApiResponse, ApiResponses}



@Api(value = "/api", description = "", produces = "application/json")
@Path("/api")
object ApiService {

  val route: Route = pathPrefix("api" / "streams") {
    pathEnd(getAll) ~ getInfo
  }

  @ApiOperation(value = "Return listing of streams", notes = "", nickname = "streams_all", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "List of Streams", response = classOf[MediaStreamsSerializer])
  ))
  def getAll: Route = get {
    utf8json {
      complete(MediaStreamsSerializer(StreamDb.all.map { case (_, stream) =>
        MediaStreamSerializer(stream, ListenerDb.countByPath(stream.path))
      }))
    }
  }

  @ApiOperation(value = "Return info about stream", notes = "", nickname = "stream_info", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Stream Info", response = classOf[MediaStreamInfoSerializer])
  ))
  def getInfo: Route = streamPath { sPath =>
    get {
      utf8json {
        complete(streamInfo(sPath))
      }
    }
  }

  private def streamInfo(path: String) = StreamDb.get(path).map(stream =>
    MediaStreamInfoSerializer((stream,
      ListenerDb.getByPath(path).map { case (_, listener) =>
        ListenerSerializer(listener)
      }))
  )
}
