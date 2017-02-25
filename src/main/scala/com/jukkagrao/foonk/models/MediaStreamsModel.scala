package com.jukkagrao.foonk.models

import io.swagger.annotations.ApiModel

@ApiModel(description = "MediaStreams list")
final case class MediaStreamsModel(streams: List[MediaStreamModel])
