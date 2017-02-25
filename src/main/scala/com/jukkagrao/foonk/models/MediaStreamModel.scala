package com.jukkagrao.foonk.models

import com.jukkagrao.foonk.streams.MediaStream
import io.swagger.annotations._

import scala.annotation.meta.field


@ApiModel(description = "MediaStream")
final case class MediaStreamModel(
                              @(ApiModelProperty@field)(value = "Stream path")
                              path: String,

                              @(ApiModelProperty@field)(value = "Stream contentType")
                              contentType: String,

                              @(ApiModelProperty@field)(value = "Stream description")
                              description: Option[String])

object MediaStreamModel {
  def apply(path: String, stream: MediaStream): MediaStreamModel = {
    MediaStreamModel(
      path,
      stream.contentType.toString(),
      stream.description
    )
  }
}




