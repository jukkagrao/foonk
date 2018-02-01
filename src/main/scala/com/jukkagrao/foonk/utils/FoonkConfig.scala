package com.jukkagrao.foonk.utils

import pureconfig.loadConfigOrThrow

import scala.concurrent.duration._

case class FoonkConfig(version: String,
                       interface: String,
                       port: Int,
                       icySupport: Boolean,
                       icyPort: Option[Int],
                       apiAuth: BasicAuth,
                       sourceAuth: BasicAuth,
                       sources: Seq[RelaySource])

case class BasicAuth(username: String, password: String)

case class RelaySource(mount: String, uri: String, onDemand: Boolean = false, retryTimeout: FiniteDuration = 1.second)

object FoonkConf {
  val conf: FoonkConfig = loadConfigOrThrow[FoonkConfig]("foonk")
}