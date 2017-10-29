package com.jukkagrao.foonk.http.api.serializers

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.jukkagrao.foonk.models.MediaStream
import io.swagger.annotations._
import spray.json.DefaultJsonProtocol

import scala.annotation.meta.field


@ApiModel(description = "MediaStream")
final case class MediaStreamSerializer(@(ApiModelProperty@field)(value = "Stream path")
                                       path: String,

                                       @(ApiModelProperty@field)(value = "ContentType")
                                       contentType: String,

                                       @(ApiModelProperty@field)(value = "Bitrate")
                                       bitrate: Option[String],

                                       @(ApiModelProperty@field)(value = "Station name")
                                       name: Option[String],

                                       @(ApiModelProperty@field)(value = "Genre")
                                       genre: Option[String],

                                       @(ApiModelProperty@field)(value = "Stream description")
                                       description: Option[String],

                                       @(ApiModelProperty@field)(value = "Station URL")
                                       url: Option[String],

                                       @(ApiModelProperty@field)(value = "Connection time")
                                       connected: String,

                                       @(ApiModelProperty@field)(value = "Amount of clients")
                                       clients: Int)

object MediaStreamSerializer extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val jsonFormat = jsonFormat9(MediaStreamSerializer.apply)

  def apply(stream: MediaStream, clients: Int = 0): MediaStreamSerializer = {
    MediaStreamSerializer(
      stream.mount,
      stream.contentType.toString(),
      stream.info.bitrate,
      stream.info.name,
      stream.info.genre,
      stream.info.description,
      stream.info.url,
      stream.connected.toIsoDateTimeString,
      clients)
  }
}

