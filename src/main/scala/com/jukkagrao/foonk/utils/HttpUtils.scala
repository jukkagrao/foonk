package com.jukkagrao.foonk.utils

import java.net.URLDecoder

import akka.http.scaladsl.model.{HttpHeader, HttpResponse}

object HttpUtils {

  def findHeader(lowercaseName: String)(implicit response: HttpResponse): Option[HttpHeader] =
    response.headers.find(h => h.is(lowercaseName))

  def urlDecode(value: String) = URLDecoder.decode(value, "UTF-8")
}
