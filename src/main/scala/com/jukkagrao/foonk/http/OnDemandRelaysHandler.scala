package com.jukkagrao.foonk.http

import akka.actor.ActorSystem
import akka.http.javadsl.server.CustomRejection
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.jukkagrao.foonk.db.StreamDb
import com.jukkagrao.foonk.http.directives.Directives._

import scala.concurrent.ExecutionContext

class NextRejection extends CustomRejection

object OnDemandRelaysHandler {

  import com.jukkagrao.foonk.utils.FoonkConf.conf

  def route(implicit ex: ExecutionContext,
            as: ActorSystem,
            mat: ActorMaterializer): Route =

    (get & streamPath) { (sPath) =>
      if (!StreamDb.exist(sPath)) {
        conf.sources.find(sPath == _.mount) foreach {
          relaySource => {
            val relayClient = new RelayClient(relaySource)
            relayClient.requestOnDemand()
          }
        }
      }
      reject
    }

}
