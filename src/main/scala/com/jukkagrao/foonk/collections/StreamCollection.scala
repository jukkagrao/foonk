package com.jukkagrao.foonk.collections

import com.jukkagrao.foonk.models.MediaStream


object StreamCollection extends BaseCollection[String, MediaStream] {

  override def remove(key: String): Option[MediaStream] = {
    entries.get(key) flatMap { stream =>
      stream.kill()
      super.remove(key)
    }
  }

}
