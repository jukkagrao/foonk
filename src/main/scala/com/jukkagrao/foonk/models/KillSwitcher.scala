package com.jukkagrao.foonk.models

import akka.stream.KillSwitch

trait KillSwitcher {

  val killSwitch: KillSwitch

  def kill(): Unit = killSwitch.shutdown()
}
