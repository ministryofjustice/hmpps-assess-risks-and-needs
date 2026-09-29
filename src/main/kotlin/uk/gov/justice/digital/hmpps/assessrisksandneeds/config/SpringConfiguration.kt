package uk.gov.justice.digital.hmpps.assessrisksandneeds.config

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.MapperFeature
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.kotlinModule

@Configuration
@EnableMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
class SpringConfiguration(private val clock: Clock) : WebMvcConfigurer {

  @Value("\${logging.uris.exclude.regex}")
  private val excludedLogUrls: String? = null

  @Bean(name = ["globalObjectMapper"])
  @Primary
  fun objectMapper(): JsonMapper = JsonMapper.builder()
    .configureForJackson2()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
    .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true)
    .configure(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    .changeDefaultPropertyInclusion { it.withValueInclusion(JsonInclude.Include.NON_ABSENT) }
    .addModule(kotlinModule())
    .build()

  @Bean
  fun createRequestData(): RequestData = RequestData(excludedLogUrls, clock)

  override fun addInterceptors(registry: InterceptorRegistry) {
    registry.addInterceptor(createRequestData())
  }
}
