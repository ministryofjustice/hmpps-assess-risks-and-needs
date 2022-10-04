package uk.gov.justice.digital.hmpps.assessrisksandneeds.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RoshRiskWidgetDto
import java.time.LocalDateTime

@AutoConfigureWebTestClient
@DisplayName("Risk widget Tests")
class WidgetControllerTest : IntegrationTestBase() {

  private val crn = "X123456"

  @Test
  fun `get risk summary by crn for external provider`() {
    webTestClient.get().uri("/risks/crn/$crn/widget")
      .headers(setAuthorisation(roles = listOf("ROLE_CRS_PROVIDER")))
      .exchange()
      .expectStatus().isOk
      .expectBody<RoshRiskWidgetDto>()
      .consumeWith {
        assertThat(it.responseBody).isEqualTo(
          RoshRiskWidgetDto(
            hasBeenCompleted = true,
            overallRisk = "VERY_HIGH",
            lastUpdated = null,
            riskInCommunity = mapOf(
              "Children" to "LOW",
              "Public" to "MEDIUM",
              "Known Adult" to "LOW",
              "Staff" to "HIGH",
            ),
          )
        )
      }
  }

  @Test
  fun `get risk summary by crn for probation practitioner`() {
    webTestClient.get().uri("/risks/crn/$crn/widget")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isOk
      .expectBody<RoshRiskWidgetDto>()
      .consumeWith {
        assertThat(it.responseBody).isEqualTo(
          RoshRiskWidgetDto(
            hasBeenCompleted = true,
            overallRisk = "VERY_HIGH",
            lastUpdated = LocalDateTime.of(2021, 6, 21, 15, 55, 4),
            riskInCommunity = mapOf(
              "Children" to "LOW",
              "Public" to "MEDIUM",
              "Known Adult" to "LOW",
              "Staff" to "HIGH",
            ),
            riskInCustody = mapOf(
              "Children" to "LOW",
              "Public" to "LOW",
              "Known Adult" to "LOW",
              "Staff" to "VERY_HIGH",
              "Prisoners" to "HIGH",
            ),
          )
        )
      }
  }

  @Test
  fun `get risk for unknown crn returns not found`() {
    webTestClient.get().uri("/risks/crn/RANDOMCRN/widget")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isOk
      .expectBody<RoshRiskWidgetDto>()
      .consumeWith {
        assertThat(it.responseBody).isEqualTo(
          RoshRiskWidgetDto(hasBeenCompleted = false)
        )
      }
  }
}
