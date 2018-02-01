package com.jukkagrao.foonk.models

import akka.NotUsed
import akka.http.scaladsl.model.{ContentType, DateTime}
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.jukkagrao.foonk.switchers.{FallbackSwitcher, SourceSwitcher}

abstract class MediaStream extends Switcher {

  val mount: String

  val source: Source[ByteString, Any]

  val contentType: ContentType

  val info: StreamInfo

  val connected: DateTime

  val fallback: FallbackSwitcher

  val switcher: SourceSwitcher

  val stream: Source[ByteString, NotUsed]

}

