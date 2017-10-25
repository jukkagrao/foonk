package com.jukkagrao.foonk.http.api

import javax.ws.rs.Path

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.jukkagrao.foonk.db.{ListenerDb, StreamDb}
import com.jukkagrao.foonk.http.api.serializers.{ListenerSerializer, MediaStreamInfoSerializer, MediaStreamSerializer, MediaStreamsSerializer}
import com.jukkagrao.foonk.http.directives.Directives._
import io.swagger.annotations._


@Api(value = "/api", description = "", produces = "application/json")
@Path("/api")
class ApiService(implicit as: ActorSystem, mat: Materializer) {

  val route: Route = pathPrefix("api") {
    pathPrefix("streams") {
      pathEnd(getAll) ~
        getInfo ~
        switchStream ~
        switchStreamBack ~
        kickStream
    } ~ kickListener
  }


  @Path("/streams")
  @ApiOperation(value = "Return listing of streams", notes = "", nickname = "streams_all", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "List of Streams", response = classOf[MediaStreamsSerializer])
  ))
  def getAll: Route = get {
    utf8json {
      complete(MediaStreamsSerializer(StreamDb.all.map { case (_, stream) =>
        MediaStreamSerializer(stream, ListenerDb.countByPath(stream.mount))
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


  @Path("/streams/{stream}")
  @ApiOperation(value = "Kick Stream", notes = "", nickname = "kick_stream", httpMethod = "DELETE")
  @ApiResponses(Array(
    new ApiResponse(code = 204, message = "Stream was kicked"),
    new ApiResponse(code = 404, message = "Stream not found")
  ))
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "stream", value = "Stream path", required = true, dataType = "string", paramType = "path")
  ))
  def kickStream: Route = streamPath { sPath =>
    delete {
      StreamDb.get(sPath) match {
        case Some(stream) =>
          stream.kill()
          complete(StatusCodes.NoContent)
        case None => complete(StatusCodes.NotFound)
      }
    }
  }


  @Path("/streams/{stream}/to/{toStream}")
  @ApiOperation(value = "Switch Stream to another one", notes = "", nickname = "switch_stream", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 201, message = "Stream was switched"),
    new ApiResponse(code = 404, message = "At least one of Streams not found")
  ))
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "stream", value = "Stream path", required = true, dataType = "string", paramType = "path"),
    new ApiImplicitParam(name = "toStream", value = "Switch to Stream path", required = true, dataType = "string", paramType = "path")
  ))
  def switchStream: Route = path(Segment / "to" / Segment) { (from, to) =>
    get((for {
      fromStream <- StreamDb.get(from)
      toStream <- StreamDb.get(to)
    } yield (fromStream, toStream)).map { case (f, t) =>
      f.switcher.switchTo(t)
      complete(StatusCodes.Created)
    }.getOrElse(complete(StatusCodes.NotFound)))
  }

  @Path("/streams/{stream}/back")
  @ApiOperation(value = "Switch Stream to initial", notes = "", nickname = "switch_stream", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 201, message = "Stream was switched"),
    new ApiResponse(code = 404, message = "Stream not found")
  ))
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "stream", value = "Stream path", required = true, dataType = "string", paramType = "path"),
  ))
  def switchStreamBack: Route = path(Segment / "back") { stream =>
    get(StreamDb.get(stream).map { s =>
      s.switcher.switchBack()
      complete(StatusCodes.Created)
    }.getOrElse(complete(StatusCodes.NotFound)))
  }


  @Path("/listeners/{id}")
  @ApiOperation(value = "Kick Listener", notes = "", nickname = "kick_listener", httpMethod = "DELETE")
  @ApiResponses(Array(
    new ApiResponse(code = 204, message = "Listener was kicked"),
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

object ApiService {
  def apply()(implicit as: ActorSystem, mat: Materializer): Route = new ApiService().route
}