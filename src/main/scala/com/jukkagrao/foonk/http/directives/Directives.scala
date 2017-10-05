package com.jukkagrao.foonk.http.directives

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ContentType, MediaTypes}
import akka.http.scaladsl.server.{Directive, Directive0, Route}
import akka.http.scaladsl.server.Directives.{path, authenticateBasic, mapResponseEntity, method, optionalHeaderValue, put, respondWithHeaders}
import com.jukkagrao.foonk.http.auth.SourceAuthenticator
import com.jukkagrao.foonk.http.headers._
import com.jukkagrao.foonk.http.methods.SourceMethod
import com.jukkagrao.foonk.streams.MediaStream

object Directives {
  def extractIceHeaders:
  Directive[(Option[String], Option[String], Option[String], Option[String], Option[String], Option[String], Option[String])] =
    optionalHeaderValue(`Ice-Name`.unapply) &
      optionalHeaderValue(`Ice-Description`.unapply) &
      optionalHeaderValue(`Ice-Genre`.unapply) &
      optionalHeaderValue(`Ice-Bitrate`.unapply) &
      optionalHeaderValue(`Ice-Audio-Info`.unapply) &
      optionalHeaderValue(`Ice-Url`.unapply) &
      optionalHeaderValue(`Ice-Public`.unapply)

  def respondWithIcyHeaders(stream: MediaStream): Directive0 = {
    val headers = Seq(`Icy-Name`, `Icy-Genre`, `Icy-Description`, `Icy-Bitrate`, `Icy-Audio-Info`, `Icy-Url`)
    val values = Seq(stream.name, stream.genre, stream.description, stream.bitrate, stream.audioInfo, stream.url)
    val headersWithValues = headers.zip(values).flatMap {
      case (header, value) => value.map(header(_))
    }

    respondWithHeaders(headersWithValues: _*)
  }

  def iceSource(route: Route)(implicit sys: ActorSystem): Route =
    (put | method(SourceMethod.method)) {
      authenticateBasic(realm = "foonk source", SourceAuthenticator.authenticator)(_ => route)
    }

  def utf8json: Directive0 = mapResponseEntity(_.withContentType(
    ContentType(MediaTypes.`application/json`
      .withParams(Map("charset" -> "utf-8"))
    )))

  def streamPath: Directive[Tuple1[String]] = path("""[\w\d\-_\.]+""".r)

}
