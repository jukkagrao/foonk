package foonk.http.headers

import akka.http.scaladsl.model.headers._

import scala.util.Try


final class `Ice-Public`(flag: String) extends ModeledCustomHeader[`Ice-Public`] {
  override def renderInRequests = false
  override def renderInResponses = false
  override val companion = `Ice-Public`
  override def value: String = flag
}
object `Ice-Public` extends ModeledCustomHeaderCompanion[`Ice-Public`] {
  override val name = "Ice-Public"
  override def parse(value: String) = Try(new `Ice-Public`(value))
}


final class `Ice-Description`(description: String) extends ModeledCustomHeader[`Ice-Description`] {
  override def renderInRequests = false
  override def renderInResponses = false
  override val companion = `Ice-Description`
  override def value: String = description
}
object `Ice-Description` extends ModeledCustomHeaderCompanion[`Ice-Description`] {
  override val name = "Ice-Description"
  override def parse(value: String) = Try(new `Ice-Description`(value))
}


final class `Ice-Url`(url: String) extends ModeledCustomHeader[`Ice-Url`] {
  override def renderInRequests = false
  override def renderInResponses = false
  override val companion = `Ice-Url`
  override def value: String = url
}
object `Ice-Url` extends ModeledCustomHeaderCompanion[`Ice-Url`] {
  override val name = "Ice-Url"
  override def parse(value: String) = Try(new `Ice-Url`(value))
}


final class `Ice-Genre`(genre: String) extends ModeledCustomHeader[`Ice-Genre`] {
  override def renderInRequests = false
  override def renderInResponses = false
  override val companion = `Ice-Genre`
  override def value: String = genre
}
object `Ice-Genre` extends ModeledCustomHeaderCompanion[`Ice-Genre`] {
  override val name = "Ice-Genre"
  override def parse(value: String) = Try(new `Ice-Genre`(value))
}


final class `Ice-Bitrate`(bitrate: String) extends ModeledCustomHeader[`Ice-Bitrate`] {
  override def renderInRequests = false
  override def renderInResponses = false
  override val companion = `Ice-Bitrate`
  override def value: String = bitrate
}
object `Ice-Bitrate` extends ModeledCustomHeaderCompanion[`Ice-Bitrate`] {
  override val name = "Ice-Bitrate"
  override def parse(value: String) = Try(new `Ice-Bitrate`(value))
}


final class `Ice-Audio-Info`(audioInfo: String) extends ModeledCustomHeader[`Ice-Audio-Info`] {
  override def renderInRequests = false
  override def renderInResponses = false
  override val companion = `Ice-Audio-Info`
  override def value: String = audioInfo
}
object `Ice-Audio-Info` extends ModeledCustomHeaderCompanion[`Ice-Audio-Info`] {
  override val name = "Ice-Audio-Info"
  override def parse(value: String) = Try(new `Ice-Audio-Info`(value))
}