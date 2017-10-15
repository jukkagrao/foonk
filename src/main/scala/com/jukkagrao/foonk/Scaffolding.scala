package com.jukkagrao.foonk

import akka.actor.{ActorSystem, Scheduler}
import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.settings.{ParserSettings, ServerSettings}
import akka.stream.ActorMaterializer
import com.jukkagrao.foonk.config.FoonkConf
import com.jukkagrao.foonk.http.methods.SourceMethod
import com.jukkagrao.foonk.proxy.OldSourceProxy

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

class Scaffolding {

  import FoonkConf.conf

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionService: ExecutionContextExecutor = system.dispatcher
  implicit val scheduler: Scheduler = system.scheduler

  val log: LoggingAdapter = system.log

  def runWebService(route: Route): Unit = {

    // add custom method to parser settings:
    val parserSettings = ParserSettings(system).withCustomMethods(SourceMethod.method)
    val serverSettings = ServerSettings(system).withParserSettings(parserSettings)

    val binding = Http().bindAndHandle(
      route,
      conf.interface,
      conf.port,
      settings = serverSettings)

    binding.onComplete {
      case Success(x) ⇒
        log.info(s"Server is listening on ${x.localAddress.getHostName}:${x.localAddress.getPort}")
      case Failure(e) ⇒
        log.warning(s"Binding failed with ${e.getMessage}")
    }

    if (conf.icySupport) {

      val proxy = new OldSourceProxy(conf.interface,
        conf.port,
        conf.icyPort.getOrElse(conf.port + 1),
        serverSettings).proxy()

      proxy.onComplete {
        case Success(x) ⇒
          log.info(s"Proxy Source Server is listening on ${x.localAddress.getHostName}:${x.localAddress.getPort}")
        case Failure(e) ⇒
          log.warning(s"Binding failed with ${e.getMessage}")
      }
    }

  }

}