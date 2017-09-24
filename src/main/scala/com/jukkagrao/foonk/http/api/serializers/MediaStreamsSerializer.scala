package com.jukkagrao.foonk.http.api.serializers

import io.swagger.annotations.ApiModel

@ApiModel(description = "MediaStreams list")
final case class MediaStreamsSerializer(streams: List[MediaStreamSerializer])
