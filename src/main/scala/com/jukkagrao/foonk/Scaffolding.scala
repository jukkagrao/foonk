package com.jukkagrao.foonk

import akka.actor.{ActorSystem, Scheduler}
import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.settings.{ParserSettings, ServerSettings}
import akka.stream.ActorMaterializer
import com.jukkagrao.foonk.http.methods.SourceMethod
import com.jukkagrao.foonk.proxy.OldSourceProxy

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

class Scaffolding {
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionService: ExecutionContextExecutor = system.dispatcher
  implicit val scheduler: Scheduler = system.scheduler

  val log: LoggingAdapter = system.log

  def runWebService(route: Route): Unit = {
    val config = system.settings.config.getConfig("foonk")
    val interface = config.getString("interface")
    val port = config.getInt("port")

    // add custom method to parser settings:
    val parserSettings = ParserSettings(system).withCustomMethods(SourceMethod.method)
    val serverSettings = ServerSettings(system).withParserSettings(parserSettings)

    val binding = Http().bindAndHandle(route, interface, port, settings = serverSettings)

    binding.onComplete {
      case Success(x) ⇒
        log.info(s"Server is listening on ${x.localAddress.getHostName}:${x.localAddress.getPort}")
      case Failure(e) ⇒
        log.warning(s"Binding failed with ${e.getMessage}")
    }

    if (config.getBoolean("icy-support")) {

      val proxy = new OldSourceProxy(interface, port, serverSettings).proxy()

      proxy.onComplete {
        case Success(x) ⇒
          log.info(s"Proxy Source Server is listening on ${x.localAddress.getHostName}:${x.localAddress.getPort}")
        case Failure(e) ⇒
          log.warning(s"Binding failed with ${e.getMessage}")
      }
    }

  }

}