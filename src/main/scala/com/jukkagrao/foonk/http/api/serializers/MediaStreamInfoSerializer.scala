package com.jukkagrao.foonk.http.api.serializers

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.jukkagrao.foonk.models.MediaStream
import io.swagger.annotations.{ApiModel, ApiModelProperty}
import spray.json.DefaultJsonProtocol

import scala.annotation.meta.field

@ApiModel(description = "MediaStream")
final case class MediaStreamInfoSerializer(@(ApiModelProperty@field)(value = "Media Stream")
                                           stream: MediaStreamSerializer,

                                           @(ApiModelProperty@field)(value = "Clients")
                                           clients: List[ClientSerializer])

object MediaStreamInfoSerializer extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val jsonFormat = jsonFormat2(MediaStreamInfoSerializer.apply)

  def apply(streamWithClients: (MediaStream, List[ClientSerializer])): MediaStreamInfoSerializer = {
    val stream = streamWithClients._1
    val clients = streamWithClients._2
    new MediaStreamInfoSerializer(
      MediaStreamSerializer(
        stream.mount,
        stream.contentType.toString(),
        stream.info.bitrate,
        stream.info.name,
        stream.info.genre,
        stream.info.description,
        stream.info.url,
        stream.connected.toIsoDateTimeString,
        clients.size),
      clients
    )
  }
}
