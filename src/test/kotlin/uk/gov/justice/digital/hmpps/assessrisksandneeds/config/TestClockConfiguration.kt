package uk.gov.justice.digital.hmpps.assessrisksandneeds.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import java.time.LocalDateTime

@TestConfiguration
class TestClockConfiguration {
  @Bean
  @Primary
  fun clock(): Clock = object : Clock() {
    override fun now() = LocalDateTime.parse("2025-01-31T12:00:01")
  }
}
