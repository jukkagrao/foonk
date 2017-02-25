package com.jukkagrao.foonk.http.api

import javax.ws.rs.Path

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import com.jukkagrao.foonk.db.StreamDb
import com.jukkagrao.foonk.models.{MediaStreamModel, MediaStreamsModel}
import io.swagger.annotations.{Api, ApiOperation, ApiResponse, ApiResponses}
import spray.json._


trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val streamFormat = jsonFormat3(MediaStreamModel.apply)
  implicit val streamsFormat = jsonFormat1(MediaStreamsModel)
}

@Api(value = "/api", description = "", produces = "application/json")
@Path("/api")
object ApiService extends JsonSupport {

  val route: Route = path("api" / "streams")(getAll)

  @ApiOperation(value = "Return listing of streams", notes = "", nickname = "streams_all", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "List of Streams", response = classOf[MediaStreamsModel])
  ))
  def getAll: Route = get {
    complete(MediaStreamsModel(StreamDb.all.map(s => MediaStreamModel(s._1, s._2))))
  }
}
