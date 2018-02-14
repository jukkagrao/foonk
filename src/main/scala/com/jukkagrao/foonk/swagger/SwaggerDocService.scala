package com.jukkagrao.foonk.swagger

import com.github.swagger.akka.SwaggerHttpService
import com.github.swagger.akka.model.Info
import com.jukkagrao.foonk.http.api.ApiService
import com.jukkagrao.foonk.utils.FoonkConf
import io.swagger.models.ExternalDocs
import io.swagger.models.auth.BasicAuthDefinition

object SwaggerDocService extends SwaggerHttpService {
  import FoonkConf.conf
  override val apiClasses = Set(classOf[ApiService])
  override val host = s"${conf.host}:${conf.port}"
  override val info = Info(version = conf.version)
  override val externalDocs = Some(new ExternalDocs("Foonk Docs", "https://github.com/jukkagrao/foonk/docs"))
  override val securitySchemeDefinitions = Map("basicAuth" -> new BasicAuthDefinition())
}