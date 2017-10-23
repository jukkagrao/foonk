package com.jukkagrao.foonk.http.headers

import akka.http.scaladsl.model.headers._

import scala.util.Try


final class `Ice-Public`(flag: String) extends ModeledCustomHeader[`Ice-Public`] {
  override def renderInRequests = true
  override def renderInResponses = false
  override val companion = `Ice-Public`
  override def value: String = flag
}
object `Ice-Public` extends ModeledCustomHeaderCompanion[`Ice-Public`] {
  override val name = "ice-public"
  override def parse(value: String) = Try(new `Ice-Public`(value))
}

final class `Ice-Name`(name: String) extends ModeledCustomHeader[`Ice-Name`] {
  override def renderInRequests = true
  override def renderInResponses = false
  override val companion = `Ice-Name`
  override def value: String = name
}
object `Ice-Name` extends ModeledCustomHeaderCompanion[`Ice-Name`] {
  override val name = "ice-name"
  override def parse(value: String) = Try(new `Ice-Name`(value))
}


final class `Ice-Description`(description: String) extends ModeledCustomHeader[`Ice-Description`] {
  override def renderInRequests = true
  override def renderInResponses = false
  override val companion = `Ice-Description`
  override def value: String = description
}
object `Ice-Description` extends ModeledCustomHeaderCompanion[`Ice-Description`] {
  override val name = "ice-description"
  override def parse(value: String) = Try(new `Ice-Description`(value))
}


final class `Ice-Url`(url: String) extends ModeledCustomHeader[`Ice-Url`] {
  override def renderInRequests = true
  override def renderInResponses = false
  override val companion = `Ice-Url`
  override def value: String = url
}
object `Ice-Url` extends ModeledCustomHeaderCompanion[`Ice-Url`] {
  override val name = "ice-url"
  override def parse(value: String) = Try(new `Ice-Url`(value))
}


final class `Ice-Genre`(genre: String) extends ModeledCustomHeader[`Ice-Genre`] {
  override def renderInRequests = true
  override def renderInResponses = false
  override val companion = `Ice-Genre`
  override def value: String = genre
}
object `Ice-Genre` extends ModeledCustomHeaderCompanion[`Ice-Genre`] {
  override val name = "ice-genre"
  override def parse(value: String) = Try(new `Ice-Genre`(value))
}


final class `Ice-Bitrate`(bitrate: String) extends ModeledCustomHeader[`Ice-Bitrate`] {
  override def renderInRequests = true
  override def renderInResponses = false
  override val companion = `Ice-Bitrate`
  override def value: String = bitrate
}
object `Ice-Bitrate` extends ModeledCustomHeaderCompanion[`Ice-Bitrate`] {
  override val name = "ice-bitrate"
  override def parse(value: String) = Try(new `Ice-Bitrate`(value))
}


final class `Ice-Audio-Info`(audioInfo: String) extends ModeledCustomHeader[`Ice-Audio-Info`] {
  override def renderInRequests = true
  override def renderInResponses = false
  override val companion = `Ice-Audio-Info`
  override def value: String = audioInfo
}
object `Ice-Audio-Info` extends ModeledCustomHeaderCompanion[`Ice-Audio-Info`] {
  override val name = "ice-audio-info"
  override def parse(value: String) = Try(new `Ice-Audio-Info`(value))
}

final class `Icy-Public`(flag: String) extends ModeledCustomHeader[`Icy-Public`] {
  override def renderInRequests = false
  override def renderInResponses = true
  override val companion = `Icy-Public`
  override def value: String = flag
}
object `Icy-Public` extends ModeledCustomHeaderCompanion[`Icy-Public`] {
  override val name = "icy-pub"
  override def parse(value: String) = Try(new `Icy-Public`(value))
}

final class `Icy-Name`(name: String) extends ModeledCustomHeader[`Icy-Name`] {
  override def renderInRequests = false
  override def renderInResponses = true
  override val companion = `Icy-Name`
  override def value: String = name
}
object `Icy-Name` extends ModeledCustomHeaderCompanion[`Icy-Name`] {
  override val name = "icy-name"
  override def parse(value: String) = Try(new `Icy-Name`(java.net.URLEncoder.encode(value, "utf-8").replace("+", "%20")))
}


final class `Icy-Description`(description: String) extends ModeledCustomHeader[`Icy-Description`] {
  override def renderInRequests = false
  override def renderInResponses = true
  override val companion = `Icy-Description`
  override def value: String = description
}
object `Icy-Description` extends ModeledCustomHeaderCompanion[`Icy-Description`] {
  override val name = "icy-description"
  override def parse(value: String) = Try(new `Icy-Description`(java.net.URLEncoder.encode(value, "utf-8").replace("+", "%20")))
}


final class `Icy-Url`(url: String) extends ModeledCustomHeader[`Icy-Url`] {
  override def renderInRequests = false
  override def renderInResponses = true
  override val companion = `Icy-Url`
  override def value: String = url
}
object `Icy-Url` extends ModeledCustomHeaderCompanion[`Icy-Url`] {
  override val name = "icy-url"
  override def parse(value: String) = Try(new `Icy-Url`(value))
}


final class `Icy-Genre`(genre: String) extends ModeledCustomHeader[`Icy-Genre`] {
  override def renderInRequests = false
  override def renderInResponses = true
  override val companion = `Icy-Genre`
  override def value: String = genre
}
object `Icy-Genre` extends ModeledCustomHeaderCompanion[`Icy-Genre`] {
  override val name = "icy-genre"
  override def parse(value: String) = Try(new `Icy-Genre`(value))
}


final class `Icy-Br`(bitrate: String) extends ModeledCustomHeader[`Icy-Br`] {
  override def renderInRequests = false
  override def renderInResponses = true
  override val companion = `Icy-Br`
  override def value: String = bitrate
}
object `Icy-Br` extends ModeledCustomHeaderCompanion[`Icy-Br`] {
  override val name = "icy-br"
  override def parse(value: String) = Try(new `Icy-Br`(value))
}


final class `Icy-Audio-Info`(audioInfo: String) extends ModeledCustomHeader[`Icy-Audio-Info`] {
  override def renderInRequests = false
  override def renderInResponses = true
  override val companion = `Icy-Audio-Info`
  override def value: String = audioInfo
}
object `Icy-Audio-Info` extends ModeledCustomHeaderCompanion[`Icy-Audio-Info`] {
  override val name = "icy-audio-info"
  override def parse(value: String) = Try(new `Icy-Audio-Info`(value))
}