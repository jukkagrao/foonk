package com.jukkagrao.foonk.listeners

import akka.NotUsed
import akka.http.scaladsl.model.{DateTime, RemoteAddress}
import akka.stream.KillSwitch
import akka.stream.scaladsl.Source
import akka.util.ByteString

trait StreamListener {

  def streamPath: String

  def id: Int

  def ip: RemoteAddress

  def stream: Source[ByteString, NotUsed]

  def connected: DateTime

  def killSwitch: KillSwitch

  def kill(): Unit = killSwitch.shutdown()

}

