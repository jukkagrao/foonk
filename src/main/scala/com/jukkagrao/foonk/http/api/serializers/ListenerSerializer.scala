package com.jukkagrao.foonk.http.api.serializers

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.jukkagrao.foonk.listeners.StreamListener
import io.swagger.annotations.{ApiModel, ApiModelProperty}
import spray.json.DefaultJsonProtocol

import scala.annotation.meta.field


@ApiModel(description = "Listener")
final case class ListenerSerializer(@(ApiModelProperty@field)(value = "ID")
                                    id: Int,

                                    @(ApiModelProperty@field)(value = "IP address")
                                    ip: String,

                                    @(ApiModelProperty@field)(value = "Connection time")
                                    connected: String)

object ListenerSerializer extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val listenerFormat = jsonFormat3(ListenerSerializer.apply)

  def apply(listener: StreamListener): ListenerSerializer =
    new ListenerSerializer(listener.id,
      listener.ip.toOption.map(_.getHostAddress).getOrElse("unknown"), listener.connected.toIsoDateTimeString)
}
