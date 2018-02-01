package com.jukkagrao.foonk.http.directives

import akka.actor.ActorSystem
import akka.http.scaladsl.model.headers.`User-Agent`
import akka.http.scaladsl.model.{ContentType, MediaTypes}
import akka.http.scaladsl.server.Directives.{authenticateBasic, extract, mapResponseEntity, method, optionalHeaderValue, path, post, put, respondWithHeaders}
import akka.http.scaladsl.server.{Directive, Directive0, Route}
import com.jukkagrao.foonk.http.auth.BasicAuthenticator
import com.jukkagrao.foonk.http.headers._
import com.jukkagrao.foonk.http.methods.SourceMethod
import com.jukkagrao.foonk.models.MediaStream
import com.jukkagrao.foonk.utils.{BasicAuth, FoonkConf}

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
    val info = stream.info
    val headers = Seq(`Icy-Name`, `Icy-Genre`, `Icy-Description`, `Icy-Br`, `Icy-Audio-Info`, `Icy-Url`)
    val values = Seq(info.name, info.genre, info.description, info.bitrate, info.audioInfo, info.url)
    val headersWithValues = headers.zip(values).flatMap {
      case (header, value) => value.map(header(_))
    }

    respondWithHeaders(headersWithValues: _*)
  }

  def iceSource(route: Route)(implicit sys: ActorSystem): Route =
    (put | post | method(SourceMethod.method)) {
      implicit val authConfig: BasicAuth = FoonkConf.conf.sourceAuth
      authenticateBasic(realm = "foonk source", BasicAuthenticator.authenticator)(_ => route)
    }

  def utf8json: Directive0 = mapResponseEntity(_.withContentType(
    ContentType(MediaTypes.`application/json`
      .withParams(Map("charset" -> "utf-8"))
    )))

  def streamPath: Directive[Tuple1[String]] = path("""[\w\d\-_\.]+""".r)

  def userAgent = extract(_.request.header[`User-Agent`])

}
