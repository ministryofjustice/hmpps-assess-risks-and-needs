package uk.gov.justice.digital.hmpps.assessrisksandneeds.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskRoshSummaryDto

@AutoConfigureWebTestClient
@DisplayName("Risk Tests")
class RiskControllerTest : IntegrationTestBase() {

  private val crn = "X123456"

  @Test
  fun `get risk by crn for external provider`() {
    webTestClient.get().uri("/risks/crn/$crn/summary")
      .headers(setAuthorisation(roles = listOf("ROLE_CRS_PROVIDER"), scopes = listOf("read")))
      .exchange()
      .expectStatus().isOk
      .expectBody<List<RiskRoshSummaryDto>>()
      .consumeWith {
        assertThat(it.responseBody).containsExactly(
          RiskRoshSummaryDto(
            riskInCommunity = mapOf(RiskLevel.HIGH to listOf("children"))
          )
        )
      }
  }

  @Test
  fun `get risk by crn for probation practitioner`() {
    webTestClient.get().uri("/risks/crn/$crn/summary")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION"), scopes = listOf("read")))
      .exchange()
      .expectStatus().isOk
      .expectBody<List<RiskRoshSummaryDto>>()
      .consumeWith {
        assertThat(it.responseBody).containsExactly(
          RiskRoshSummaryDto(
            "whoisAtRisk",
            "natureOfRisk",
            "riskImminence",
            "riskIncreaseFactors",
            "riskMitigationFactors",
            mapOf(RiskLevel.HIGH to listOf("children")),
            mapOf(RiskLevel.MEDIUM to listOf("known adult"))
          )
        )
      }
  }
}
