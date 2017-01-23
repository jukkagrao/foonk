package foonk.streams

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.ContentType
import akka.stream.{Materializer, SharedKillSwitch}
import akka.stream.scaladsl.Source
import akka.util.ByteString
import foonk.utils.SourceSwitcher

import scala.concurrent.ExecutionContext


trait MediaStream {

  def path: String

  def source: Source[ByteString, Any]

  def contentType: ContentType

  def genre: Option[String]

  def description: Option[String]

  def public: Boolean

  def url: Option[String]

  def bitrate: Option[String]

  def switcher(implicit sys: ActorSystem,
               mat: Materializer,
               ev: ExecutionContext) = SourceSwitcher(this)

  def stream: Source[ByteString, NotUsed]

  def killSwitch: SharedKillSwitch

  def kill(): Unit = killSwitch.shutdown()

}
