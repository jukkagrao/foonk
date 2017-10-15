package com.jukkagrao.foonk.config

import pureconfig.loadConfigOrThrow

case class FoonkConfig(version: String,
                       interface: String,
                       port: Int,
                       icySupport: Boolean,
                       icyPort: Option[Int],
                       sourceAuth: SourceAuth,
                       sources: Seq[Source])

case class SourceAuth(username: String = "source", password: String)

case class Source(mount: String, url: String, onDemand: Boolean = false)

object FoonkConf {
  val conf: FoonkConfig = loadConfigOrThrow[FoonkConfig]("foonk")
}