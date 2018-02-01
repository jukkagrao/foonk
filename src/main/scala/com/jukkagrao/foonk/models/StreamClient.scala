package com.jukkagrao.foonk.models

import akka.NotUsed
import akka.http.scaladsl.model.headers.`User-Agent`
import akka.http.scaladsl.model.{DateTime, RemoteAddress}
import akka.stream.scaladsl.Source
import akka.util.ByteString

abstract class StreamClient extends Switcher {

  def streamPath: String

  def id: Int

  def ip: RemoteAddress

  def userAgent: Option[`User-Agent`]

  def stream: Source[ByteString, NotUsed]

  def connected: DateTime

}

