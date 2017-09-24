package com.jukkagrao.foonk.proxy

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.settings.ServerSettings
import akka.stream.Materializer
import akka.stream.scaladsl.Tcp.ServerBinding
import akka.stream.scaladsl.{Flow, Sink, Source, Tcp}
import akka.util.ByteString

import scala.concurrent.Future
import scala.concurrent.duration.Duration

/** *
  *
  * This proxy need to rewrite old-fashion ICY protocol to HTTP/1.1
  *
  * It listen incoming connections on port + 1
  *
  * @param interface network interface of stream server
  * @param port      of stream server
  * @param settings  ServerSettings
  */

class OldSourceProxy(interface: String, port: Int, settings: ServerSettings)
                    (implicit system: ActorSystem, mat: Materializer) {

  private def bind = {
    Tcp()
      .bind(interface,
        port + 1,
        settings.backlog,
        settings.socketOptions,
        halfClose = false,
        idleTimeout = Duration.Inf)
  }

  def proxy(): Future[ServerBinding] = {
    bind.map {
      connection =>
        connection handleWith flow
    }.to(Sink.ignore).run()
  }

  private def outgoingConnection = {
    Tcp().outgoingConnection(interface, port)
  }

  val flow: Flow[ByteString, ByteString, NotUsed] = Flow[ByteString].prefixAndTail(1)
    .flatMapConcat { case (head, tail) =>
      Source(head)
        .map(rewriteRequestHeaders)
        .concat(tail
          .map(toChunkedEncoding)
        )
    }.via(outgoingConnection.map(rewriteResponseHeaders))


  private def rewriteRequestHeaders(headers: ByteString) = {
    val methodProtocolEncoding = "SOURCE (.*) (?:HTTP|ICE)/1.(?:0|1)"
      .r
      .replaceFirstIn(headers.utf8String, "PUT $1 HTTP/1.1\r\nExpect: 100-Continue\r\nTransfer-Encoding: chunked")

    val host = if (methodProtocolEncoding.contains("Host"))
      methodProtocolEncoding
    else methodProtocolEncoding.replace("Expect: 100-Continue", s"Host: $interface:$port\r\nExpect: 100-Continue")

    ByteString.fromString(host)
  }

  private def rewriteResponseHeaders(headers: ByteString) = {
    val headersString = headers.utf8String
    val result = if (headersString.contains("HTTP/1.1 100 Continue"))
      "HTTP/1.0 200 OK\n\n"
    else headersString

    ByteString.fromString(result)
  }

  private def toChunkedEncoding(chunk: ByteString): ByteString = {
    ByteString.fromString(chunk.length.toHexString + "\r\n") ++ chunk ++ ByteString.fromString("\r\n")
  }
}

