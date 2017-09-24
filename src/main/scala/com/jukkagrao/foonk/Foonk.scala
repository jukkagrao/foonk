package com.jukkagrao.foonk

import akka.http.scaladsl.server.Directives._
import com.jukkagrao.foonk.http.api.ApiService
import com.jukkagrao.foonk.http.{IncomingSourceHandler, ListenersHandler}


object Foonk extends Scaffolding with App {

  runWebService {
    ApiService.route ~
    ListenersHandler.route ~
    IncomingSourceHandler()
  }

}



