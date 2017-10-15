package com.jukkagrao.foonk.utils

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}

trait Logger {
  def log(implicit as: ActorSystem): LoggingAdapter = Logging.getLogger(as, this)
}
