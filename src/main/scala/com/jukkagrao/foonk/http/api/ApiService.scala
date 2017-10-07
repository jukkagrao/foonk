package com.jukkagrao.foonk.http.api

import javax.ws.rs.Path

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.jukkagrao.foonk.db.{ListenerDb, StreamDb}
import com.jukkagrao.foonk.http.api.serializers.{ListenerSerializer, MediaStreamInfoSerializer, MediaStreamSerializer, MediaStreamsSerializer}
import com.jukkagrao.foonk.http.directives.Directives._
import io.swagger.annotations._


@Api(value = "/api", description = "", produces = "application/json")
@Path("/api")
object ApiService {

  val route: Route = pathPrefix("api") {
    pathPrefix("streams") {
      pathEnd(getAll) ~
        getInfo
    } ~ pathPrefix("listeners") {
      kickListener
    }
  }


  @Path("/streams")
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


  @Path("/streams/{stream}")
  @ApiOperation(value = "Return info about stream", notes = "", nickname = "stream_info", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Stream Info", response = classOf[MediaStreamInfoSerializer]),
    new ApiResponse(code = 404, message = "Stream not found")
  ))
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "stream", value = "Stream path", required = true, dataType = "string", paramType = "path")
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


  @Path("/listeners/{id}")
  @ApiOperation(value = "Kick listener", notes = "", nickname = "kick_listener", httpMethod = "DELETE")
  @ApiResponses(Array(
    new ApiResponse(code = 204, message = "Listener kicked"),
    new ApiResponse(code = 404, message = "Listener not found")
  ))
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", value = "Listener Id", required = true, dataType = "integer", paramType = "path")
  ))
  def kickListener: Route = path("listeners" / IntNumber) { id =>
    delete {
      ListenerDb.get(id) match {
        case Some(listener) =>
          listener.kill()
          complete(StatusCodes.NoContent)
        case None => complete(StatusCodes.NotFound)
      }
    }
  }
}
