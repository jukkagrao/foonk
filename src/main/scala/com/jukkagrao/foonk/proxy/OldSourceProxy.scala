package com.jukkagrao.foonk.proxy

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.settings.ServerSettings
import akka.stream.Materializer
import akka.stream.scaladsl.Tcp.ServerBinding
import akka.stream.scaladsl.{Flow, Framing, Sink, Source, Tcp}
import akka.util.ByteString
import com.ibm.icu.text.CharsetDetector

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Success, Try}

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

class OldSourceProxy(interface: String, port: Int, proxyPort: Int, settings: ServerSettings)
                    (implicit system: ActorSystem, mat: Materializer) {

  private def bind = {
    Tcp()
      .bind(interface,
        proxyPort,
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

  private val outgoingConnection = {
    Tcp().outgoingConnection(interface, port)
  }

  val flow: Flow[ByteString, ByteString, NotUsed] = Flow[ByteString].prefixAndTail(1)
    .flatMapConcat { case (head, tail) =>
      Source(head)
        .map(detectCharset)
        .map(rewriteRequestHeaders)
        .concat(tail
          .map(toChunkedEncoding)
        )
    }.via(outgoingConnection.map(rewriteResponseHeaders))

  private def detectCharset(headers: ByteString) = {
    val bytes: Future[ByteString] = Source(headers :: Nil).via(Framing.delimiter(ByteString("\n"), Int.MaxValue))
      .filter(bs => bs.utf8String.contains("description") || bs.utf8String.contains("name"))
      .map(_.dropWhile(_ != ':'.toByte)).runFold(ByteString(""))((u, z) => z.concat(u))

    val btsToDetect: ByteString = Try(Await.result(bytes, Duration.Inf)) match {
      case Success(b: ByteString) => b
      case _ => ByteString("")
    }

    val detector = new CharsetDetector()
    val charset = Try(detector.setText(btsToDetect.toArray).detect().getName).getOrElse("UTF-8")

    system.log.debug(s"charset detected: $charset")

    val text = headers.decodeString(charset)
    ByteString.fromString(text)
  }

  private def rewriteRequestHeaders(headers: ByteString) = {
    val methodProtocol = "(?:SOURCE|PUT) (.*) (?:HTTP|ICE)/1.(?:0|1)"
      .r
      .replaceFirstIn(headers.utf8String, "PUT $1 HTTP/1.1\r\nExpect: 100-Continue")

    val encoding = if (methodProtocol.contains("Transfer-Encoding"))
      methodProtocol
    else methodProtocol.replace("Expect: 100-Continue", "Expect: 100-Continue\r\nTransfer-Encoding: chunked")

    val host = if (encoding.contains("Host"))
      encoding
    else encoding.replace("Expect: 100-Continue", s"Host: $interface:$port\r\nExpect: 100-Continue")

    system.log.debug(host)

    ByteString.fromString(new String(host.getBytes(), "UTF-8"))
  }

  private def rewriteResponseHeaders(headers: ByteString) = {
    val headersString = headers.utf8String
    val result = if (headersString.contains("HTTP/1.1 100 Continue"))
      "HTTP/1.1 100 Continue\r\n\r\nHTTP/1.0 200 OK\r\n\r\n"
    else headersString

    ByteString.fromString(result)
  }

  private def toChunkedEncoding(chunk: ByteString): ByteString = {
    ByteString.fromString(chunk.length.toHexString + "\r\n") ++ chunk ++ ByteString.fromString("\r\n")
  }
}

