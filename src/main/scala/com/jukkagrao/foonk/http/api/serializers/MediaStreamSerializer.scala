package com.jukkagrao.foonk.http.api.serializers

import com.jukkagrao.foonk.streams.MediaStream
import io.swagger.annotations._

import scala.annotation.meta.field


@ApiModel(description = "MediaStream")
final case class MediaStreamSerializer(
                              @(ApiModelProperty@field)(value = "Stream path")
                              path: String,

                              @(ApiModelProperty@field)(value = "Stream contentType")
                              contentType: String,

                              @(ApiModelProperty@field)(value = "Stream description")
                              description: Option[String])

object MediaStreamSerializer {
  def apply(path: String, stream: MediaStream): MediaStreamSerializer = {
    MediaStreamSerializer(
      path,
      stream.contentType.toString(),
      stream.description
    )
  }
}

