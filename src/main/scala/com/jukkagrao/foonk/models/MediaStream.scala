package com.jukkagrao.foonk.models

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ContentType, DateTime}
import akka.stream.{Materializer, SharedKillSwitch}
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.jukkagrao.foonk.utils.SourceSwitcher

import scala.concurrent.ExecutionContext


trait MediaStream {

  def path: String

  def source: Source[ByteString, Any]

  def contentType: ContentType

  def name: Option[String]

  def description: Option[String]

  def genre: Option[String]

  def public: Boolean

  def bitrate: Option[String]

  def url: Option[String]

  def audioInfo: Option[String]

  def connected: DateTime

  def switcher(implicit sys: ActorSystem,
               mat: Materializer,
               ev: ExecutionContext) = SourceSwitcher(this)

  def stream: Source[ByteString, NotUsed]

  def killSwitch: SharedKillSwitch

  def kill(): Unit = killSwitch.shutdown()

}
