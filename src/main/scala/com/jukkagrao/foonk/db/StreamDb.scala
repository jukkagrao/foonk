package com.jukkagrao.foonk.db

import com.jukkagrao.foonk.models.MediaStream


object StreamDb extends BaseDb[String, MediaStream] {

  override def remove(key: String): Option[MediaStream] = {
    entries.get(key) flatMap { stream =>
      stream.kill()
      super.remove(key)
    }
  }

}
