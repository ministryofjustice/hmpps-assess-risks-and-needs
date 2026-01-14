package uk.gov.justice.digital.hmpps.assessrisksandneeds.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.LocalDateTime

open class Clock {
  open fun now(): LocalDateTime = LocalDateTime.now()
}

@Configuration
class ClockConfiguration {
  @Bean
  fun clock() = Clock()
}
