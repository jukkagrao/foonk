package com.jukkagrao.foonk

import akka.http.scaladsl.server.Directives._
import com.jukkagrao.foonk.http.api.ApiService
import com.jukkagrao.foonk.http.{ClientsHandler, IncomingSourceHandler}
import com.jukkagrao.foonk.swagger.SwaggerDocService


object Foonk extends Scaffolding with App {

  runWebService {
    ApiService() ~
      ClientsHandler.route ~
      IncomingSourceHandler() ~
      SwaggerDocService.routes
  }

}



