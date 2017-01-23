package foonk

import akka.http.scaladsl.server.Directives._
import foonk.http.{IncomingSourceHandler, ListenersHandler}


object Foonk extends Scaffolding with App {

  runWebService {
    ListenersHandler.route ~
      IncomingSourceHandler()
  }

}



