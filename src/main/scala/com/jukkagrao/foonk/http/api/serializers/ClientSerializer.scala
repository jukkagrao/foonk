package com.jukkagrao.foonk.http.api.serializers

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.jukkagrao.foonk.models.StreamClient
import io.swagger.annotations.{ApiModel, ApiModelProperty}
import spray.json.DefaultJsonProtocol

import scala.annotation.meta.field


@ApiModel(description = "Client")
final case class ClientSerializer(@(ApiModelProperty@field)(value = "ID")
                                    id: Int,

                                  @(ApiModelProperty@field)(value = "IP address")
                                    ip: String,

                                  @(ApiModelProperty@field)(value = "User Agent")
                                    userAgent: String,

                                  @(ApiModelProperty@field)(value = "Connection time")
                                    connected: String)

object ClientSerializer extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val jsonFormat = jsonFormat4(ClientSerializer.apply)

  def apply(client: StreamClient): ClientSerializer =
    new ClientSerializer(client.id,
      client.ip.toOption.map(_.getHostAddress).getOrElse("unknown"),
      client.userAgent.map(_.value).getOrElse("unknown"),
      client.connected.toIsoDateTimeString)
}
