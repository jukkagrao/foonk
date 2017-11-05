package com.jukkagrao.foonk.http.auth

import akka.actor.ActorSystem
import akka.http.scaladsl.server.directives.Credentials
import com.jukkagrao.foonk.utils.FoonkConf

object SourceAuthenticator {
  import FoonkConf.conf

  def authenticator(credentials: Credentials)(implicit sys: ActorSystem): Option[String] = {
    val password = conf.sourceAuth.password

    credentials match {
      case p@Credentials.Provided(id) if p.verify(password) => Some(id)
      case _ => None
    }
  }
}
