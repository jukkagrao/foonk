package com.jukkagrao.foonk.http.auth

import akka.actor.ActorSystem
import akka.http.scaladsl.server.directives.Credentials
import com.jukkagrao.foonk.utils.BasicAuth

object BasicAuthenticator {

  def authenticator(authConfig: BasicAuth)(credentials: Credentials)(implicit sys: ActorSystem): Option[String] = {
    val password = authConfig.password
    val username = authConfig.username

    credentials match {
      case p@Credentials.Provided(uname) if uname == username && p.verify(password) => Some(uname)
      case _ => None
    }
  }
}
