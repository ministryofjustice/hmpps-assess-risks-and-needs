package uk.gov.justice.digital.hmpps.assessrisksandneeds.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springdoc.core.utils.SpringDocUtils
import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Configuration
class OpenApiConfiguration(buildProperties: BuildProperties, clock: Clock) {
  private val version: String = buildProperties.version

  init {
    val schema: Schema<LocalDateTime> = Schema<LocalDateTime>()
    schema.example(clock.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))
    SpringDocUtils.getConfig().replaceWithSchema(LocalDateTime::class.java, schema)
  }

  @Bean
  fun customOpenAPI(buildProperties: BuildProperties): OpenAPI? = OpenAPI()
    .info(
      Info().title("HMPPS Risks and Needs API").version(version).description(
        "API for managing risks and needs",
      )
        .contact(Contact().name("HMPPS Digital Studio").email("feedback@digital.justice.gov.uk")),
    )
    .components(
      Components().addSecuritySchemes(
        "bearer-jwt",
        SecurityScheme()
          .type(SecurityScheme.Type.HTTP)
          .scheme("bearer")
          .bearerFormat("JWT")
          .`in`(SecurityScheme.In.HEADER)
          .name("Authorization"),
      ).addRiskActuarualSchemas(),
    )
    .addSecurityItem(SecurityRequirement().addList("bearer-jwt"))
}
