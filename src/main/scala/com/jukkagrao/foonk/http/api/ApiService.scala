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
        setupFallback ~
        removeFallback ~
        kickStream
    } ~ kickListener
  } ~ admin


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


  @Path("/streams/{stream}/switch/{toStream}")
  @ApiOperation(value = "Switch Stream to another one", notes = "", nickname = "switch_stream", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 201, message = "Stream was switched"),
    new ApiResponse(code = 208, message = "Mount already uses that source"),
    new ApiResponse(code = 404, message = "At least one of Streams not found"),
    new ApiResponse(code = 417, message = "Mounts have different Content-Types")
  ))
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "stream", value = "Stream path", required = true, dataType = "string", paramType = "path"),
    new ApiImplicitParam(name = "toStream", value = "Switch to Stream path", required = true, dataType = "string", paramType = "path")
  ))
  def switchStream: Route = path(Segment / "switch" / Segment) { (from, to) =>
    get((for {
      fromStream <- StreamDb.get(from)
      toStream <- StreamDb.get(to)
    } yield complete(fromStream.switcher.switchTo(toStream))).getOrElse(complete(StatusCodes.NotFound)))
  }


  @Path("/streams/{stream}/switch")
  @ApiOperation(value = "Switch Stream to initial", notes = "", nickname = "switch_to_init", httpMethod = "DELETE")
  @ApiResponses(Array(
    new ApiResponse(code = 201, message = "Stream was switched back"),
    new ApiResponse(code = 208, message = "Mount already uses that source"),
    new ApiResponse(code = 404, message = "Stream not found")
  ))
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "stream", value = "Stream path", required = true, dataType = "string", paramType = "path"),
  ))
  def switchStreamBack: Route = path(Segment / "switch") { stream =>
    delete(StreamDb.get(stream).map { s =>
      complete(s.switcher.switchBack())
    }.getOrElse(complete(StatusCodes.NotFound)))
  }


  @Path("/streams/{stream}/fallback/{fallbackStream}")
  @ApiOperation(value = "Set Stream fallback up", notes = "", nickname = "fallback_stream", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 201, message = "Fallback was set up"),
    new ApiResponse(code = 409, message = "Mount and fallback are the same streams"),
    new ApiResponse(code = 404, message = "At least one of Streams not found"),
    new ApiResponse(code = 417, message = "Mount and fallback have different Content-Types")
  ))
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "stream", value = "Stream path", required = true, dataType = "string", paramType = "path"),
    new ApiImplicitParam(name = "fallbackStream", value = "Fallback Stream path", required = true, dataType = "string", paramType = "path")
  ))
  def setupFallback: Route = path(Segment / "fallback" / Segment) { (mount, fallback) =>
    get((for {
      mountStream <- StreamDb.get(mount)
      fallbackStream <- StreamDb.get(fallback)
    } yield complete(mountStream.switcher.setFallback(fallbackStream)))
      .getOrElse(complete(StatusCodes.NotFound)))
  }


  @Path("/streams/{stream}/fallback")
  @ApiOperation(value = "Remove Stream fallback", notes = "", nickname = "fallback_stream", httpMethod = "DELETE")
  @ApiResponses(Array(
    new ApiResponse(code = 201, message = "Fallback was removed"),
    new ApiResponse(code = 404, message = "Stream or fallback not found")
  ))
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "stream", value = "Stream path", required = true, dataType = "string", paramType = "path"),
  ))
  def removeFallback: Route = path(Segment / "fallback") { mount =>
    delete((for {
      mountStream <- StreamDb.get(mount)
    } yield complete(mountStream.switcher.removeFallback()))
      .getOrElse(complete(StatusCodes.NotFound)))
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

  def admin: Route = pathPrefix("admin") {
    get {complete(StatusCodes.OK)}
  }
}

object ApiService {
  def apply()(implicit as: ActorSystem, mat: Materializer): Route = new ApiService().route
}