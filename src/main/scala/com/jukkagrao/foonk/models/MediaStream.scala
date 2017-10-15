package com.jukkagrao.foonk.models

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ContentType, DateTime}
import akka.stream.scaladsl.Source
import akka.stream.{Materializer, SharedKillSwitch}
import akka.util.ByteString
import com.jukkagrao.foonk.utils.SourceSwitcher

import scala.concurrent.ExecutionContext


trait MediaStream {

  val mount: String

  def source: Source[ByteString, Any]

  def contentType: ContentType

  val info: StreamInfo

  def connected: DateTime

  def switcher(implicit sys: ActorSystem,
               mat: Materializer,
               ev: ExecutionContext) = SourceSwitcher(this)

  def stream: Source[ByteString, NotUsed]

  def killSwitch: SharedKillSwitch

  def kill(): Unit = killSwitch.shutdown()

}
