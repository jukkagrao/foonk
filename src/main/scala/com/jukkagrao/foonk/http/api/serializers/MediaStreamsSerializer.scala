package com.jukkagrao.foonk.http.api.serializers

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import io.swagger.annotations.ApiModel
import spray.json.DefaultJsonProtocol

@ApiModel(description = "MediaStreams list")
final case class MediaStreamsSerializer(streams: List[MediaStreamSerializer])

object MediaStreamsSerializer extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val jsonFormat = jsonFormat1(MediaStreamsSerializer.apply)
}