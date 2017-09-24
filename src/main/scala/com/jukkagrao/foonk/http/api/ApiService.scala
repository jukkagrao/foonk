package com.jukkagrao.foonk.http.api

import javax.ws.rs.Path

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import com.jukkagrao.foonk.db.StreamDb
import com.jukkagrao.foonk.http.api.serializers.{MediaStreamSerializer, MediaStreamsSerializer}
import io.swagger.annotations.{Api, ApiOperation, ApiResponse, ApiResponses}
import spray.json._


trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val streamFormat: RootJsonFormat[MediaStreamSerializer] = jsonFormat3(MediaStreamSerializer.apply)
  implicit val streamsFormat: RootJsonFormat[MediaStreamsSerializer] = jsonFormat1(MediaStreamsSerializer)
}

@Api(value = "/api", description = "", produces = "application/json")
@Path("/api")
object ApiService extends JsonSupport {

  val route: Route = path("api" / "streams")(getAll)

  @ApiOperation(value = "Return listing of streams", notes = "", nickname = "streams_all", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "List of Streams", response = classOf[MediaStreamsSerializer])
  ))
  def getAll: Route = get {
    complete(MediaStreamsSerializer(StreamDb.all.map(s => MediaStreamSerializer(s._1, s._2))))
  }
}
