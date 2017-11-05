package com.jukkagrao.foonk.models

case class StreamInfo(name: Option[String] = None,
                      description: Option[String] = None,
                      genre: Option[String] = None,
                      bitrate: Option[String] = None,
                      url: Option[String] = None,
                      audioInfo: Option[String] = None,
                      public: Boolean = false)



