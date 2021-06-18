package uk.gov.justice.digital.hmpps.assessrisksandneeds.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AllRoshRiskDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OtherRoshRisksDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ResponseDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskRoshSummaryDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RoshRiskToSelfDto

@AutoConfigureWebTestClient
@DisplayName("Risk Tests")
class RiskControllerTest : IntegrationTestBase() {

  private val crn = "X123456"

  @Test
  fun `get risk summary by crn for external provider`() {
    webTestClient.get().uri("/risks/crn/$crn/summary")
      .headers(setAuthorisation(roles = listOf("ROLE_CRS_PROVIDER"), scopes = listOf("read")))
      .exchange()
      .expectStatus().isOk
      .expectBody<RiskRoshSummaryDto>()
      .consumeWith {
        assertThat(it.responseBody).isEqualTo(
          RiskRoshSummaryDto(
            riskInCommunity = mapOf(RiskLevel.HIGH to listOf("children"))
          )
        )
      }
  }

  @Test
  fun `get risk summary by crn for probation practitioner`() {
    webTestClient.get().uri("/risks/crn/$crn/summary")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION"), scopes = listOf("read")))
      .exchange()
      .expectStatus().isOk
      .expectBody<RiskRoshSummaryDto>()
      .consumeWith {
        assertThat(it.responseBody).isEqualTo(
          RiskRoshSummaryDto(
            "whoisAtRisk",
            "natureOfRisk",
            "riskImminence",
            "riskIncreaseFactors",
            "riskMitigationFactors",
            mapOf(RiskLevel.HIGH to listOf("children")),
            mapOf(RiskLevel.MEDIUM to listOf("known adult"), RiskLevel.VERY_HIGH to listOf("public", "staff"))
          )
        )
      }
  }

  @Test
  fun `get risk to self by crn for external provider`() {
    webTestClient.get().uri("/risks/crn/$crn/self")
      .headers(setAuthorisation(roles = listOf("ROLE_CRS_PROVIDER"), scopes = listOf("read")))
      .exchange()
      .expectStatus().isOk
      .expectBody<RoshRiskToSelfDto>()
      .consumeWith {
        assertThat(it.responseBody).isEqualTo(
          RoshRiskToSelfDto(
            RiskDto(ResponseDto.DK, "Previous concerns", "Current concerns"),
            RiskDto(ResponseDto.NO, "Previous concerns", "Current concerns"),
            RiskDto(ResponseDto.YES, "Previous concerns", "Current concerns"),
            RiskDto(ResponseDto.DK, "Previous concerns", "Current concerns"),
            RiskDto(null, null, null),
          )
        )
      }
  }

  @Test
  fun `get risk to self by crn for probation practitioner`() {
    webTestClient.get().uri("/risks/crn/$crn/self")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION"), scopes = listOf("read")))
      .exchange()
      .expectStatus().isOk
      .expectBody<RoshRiskToSelfDto>()
      .consumeWith {
        assertThat(it.responseBody).isEqualTo(
          RoshRiskToSelfDto(
            RiskDto(ResponseDto.DK, "Previous concerns", "Current concerns"),
            RiskDto(ResponseDto.NO, "Previous concerns", "Current concerns"),
            RiskDto(ResponseDto.YES, "Previous concerns", "Current concerns"),
            RiskDto(ResponseDto.DK, "Previous concerns", "Current concerns"),
            RiskDto(null, null, null),
          )
        )
      }
  }

  @Test
  fun `get other risks by crn for external provider`() {
    webTestClient.get().uri("/risks/crn/$crn/other")
      .headers(setAuthorisation(roles = listOf("ROLE_CRS_PROVIDER"), scopes = listOf("read")))
      .exchange()
      .expectStatus().isOk
      .expectBody<OtherRoshRisksDto>()
      .consumeWith {
        assertThat(it.responseBody).isEqualTo(
          OtherRoshRisksDto(
            null, null, null
          )
        )
      }
  }

  @Test
  fun `get other risk by crn for probation practitioner`() {
    webTestClient.get().uri("/risks/crn/$crn/other")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION"), scopes = listOf("read")))
      .exchange()
      .expectStatus().isOk
      .expectBody<OtherRoshRisksDto>()
      .consumeWith {
        assertThat(it.responseBody).isEqualTo(
          OtherRoshRisksDto(
            RiskDto(ResponseDto.YES, "Previous concerns", "Current concerns"),
            RiskDto(ResponseDto.DK, "Previous concerns", "Current concerns"),
            RiskDto(null, null, null),
          )
        )
      }
  }

  @Test
  fun `get all risks by crn for external provider`() {
    webTestClient.get().uri("/risks/crn/$crn")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION"), scopes = listOf("read")))
      .exchange()
      .expectStatus().isOk
      .expectBody<AllRoshRiskDto>()
      .consumeWith {
        assertThat(it.responseBody).isEqualTo(
          AllRoshRiskDto(
            RoshRiskToSelfDto(
              RiskDto(ResponseDto.DK, "Previous concerns", "Current concerns"),
              RiskDto(ResponseDto.NO, "Previous concerns", "Current concerns"),
              RiskDto(ResponseDto.YES, "Previous concerns", "Current concerns"),
              RiskDto(ResponseDto.DK, "Previous concerns", "Current concerns"),
              RiskDto(null, null, null),
            ),
            OtherRoshRisksDto(
              RiskDto(ResponseDto.YES, "Previous concerns", "Current concerns"),
              RiskDto(ResponseDto.DK, "Previous concerns", "Current concerns"),
              RiskDto(null, null, null)
            ),
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
        )
      }
  }
}
