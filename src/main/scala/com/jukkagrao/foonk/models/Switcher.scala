package com.jukkagrao.foonk.models

import akka.stream.KillSwitch

trait Switcher {

  val killSwitch: KillSwitch

  def kill(): Unit = killSwitch.shutdown()
}
