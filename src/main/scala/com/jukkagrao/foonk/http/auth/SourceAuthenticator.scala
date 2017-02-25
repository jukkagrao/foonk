package com.jukkagrao.foonk.http.auth

import akka.actor.ActorSystem
import akka.http.scaladsl.server.directives.Credentials

object SourceAuthenticator {

  def authenticator(credentials: Credentials)(implicit sys: ActorSystem): Option[String] = {
    val config = sys.settings.config.getConfig("foonk")
    val password = config.getString("source.password")

    credentials match {
      case p @ Credentials.Provided(id) if p.verify(password) => Some(id)
      case _ => None
    }
  }
}
