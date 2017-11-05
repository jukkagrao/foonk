package com.jukkagrao.foonk.utils

import java.net.URLDecoder

import akka.http.scaladsl.model.HttpHeader

object HttpUtils {

  def findHeader(lowercaseName: String)(implicit headers: Seq[HttpHeader]): Option[HttpHeader] =
    headers.find(h => h.is(lowercaseName))

  def urlDecode(value: String) = URLDecoder.decode(value, "UTF-8")
}
