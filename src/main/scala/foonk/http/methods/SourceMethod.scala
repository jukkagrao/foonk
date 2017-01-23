package foonk.http.methods

import akka.http.scaladsl.model.HttpMethod
import akka.http.scaladsl.model.RequestEntityAcceptance.Expected

object SourceMethod {
  val method: HttpMethod = HttpMethod.custom("SOURCE", safe = false, idempotent = true, Expected)
}
