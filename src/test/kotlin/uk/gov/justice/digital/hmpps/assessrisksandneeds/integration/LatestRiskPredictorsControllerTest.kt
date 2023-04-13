package uk.gov.justice.digital.hmpps.assessrisksandneeds.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OgpScoreDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OgrScoreDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OspScoreDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OvpScoreDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskScoresDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrScoreDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrScoreSource
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.ApiErrorResponse
import java.math.BigDecimal
import java.time.LocalDateTime

@AutoConfigureWebTestClient(timeout = "360000000")
class LatestRiskPredictorsControllerTest : IntegrationTestBase() {

  @Test
  fun `should return risk data for valid crn`() {
    // Given
    val crn = "X123456"

    // When
    webTestClient.get()
      .uri("/risks/crn/$crn/predictors/all")
      .header("Content-Type", "application/json")
      .headers(setAuthorisation(user = "assess-risks-needs", roles = listOf("ROLE_PROBATION")))
      .exchange()
      // Then
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody<List<RiskScoresDto>>()
      .consumeWith {
        assertThat(it.responseBody).hasSize(3)
        assertThat(it.responseBody[0]).usingRecursiveComparison()
          .isEqualTo(
            RiskScoresDto(
              completedDate = LocalDateTime.of(2022, 6, 10, 18, 23, 20),
              assessmentStatus = "COMPLETE",
              groupReconvictionScore = OgrScoreDto(
                oneYear = BigDecimal.valueOf(3),
                twoYears = BigDecimal.valueOf(5),
                scoreLevel = ScoreLevel.LOW,
              ),
              violencePredictorScore = OvpScoreDto(
                ovpStaticWeightedScore = BigDecimal.valueOf(14),
                ovpDynamicWeightedScore = BigDecimal.valueOf(3),
                ovpTotalWeightedScore = BigDecimal.valueOf(17),
                oneYear = BigDecimal.valueOf(4),
                twoYears = BigDecimal.valueOf(7),
                ovpRisk = ScoreLevel.LOW,
              ),
              generalPredictorScore = OgpScoreDto(
                ogpStaticWeightedScore = BigDecimal.valueOf(3),
                ogpDynamicWeightedScore = BigDecimal.valueOf(7),
                ogpTotalWeightedScore = BigDecimal.valueOf(10),
                ogp1Year = BigDecimal.valueOf(4),
                ogp2Year = BigDecimal.valueOf(8),
                ogpRisk = ScoreLevel.LOW,
              ),
              riskOfSeriousRecidivismScore = RsrScoreDto(
                percentageScore = BigDecimal.valueOf(50.1234),
                staticOrDynamic = ScoreType.DYNAMIC,
                source = RsrScoreSource.OASYS,
                algorithmVersion = "11",
                ScoreLevel.MEDIUM,
              ),
              sexualPredictorScore = OspScoreDto(
                ospIndecentPercentageScore = BigDecimal.valueOf(2.81),
                ospContactPercentageScore = BigDecimal.valueOf(1.07),
                ospIndecentScoreLevel = ScoreLevel.MEDIUM,
                ospContactScoreLevel = ScoreLevel.MEDIUM,
              ),
            ),
          )
      }
  }

  @Test
  fun `should return not found error for invalid crn`() {
    webTestClient.get().uri("/risks/crn/X999999/predictors/all")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `should return forbidden when user has insufficient privileges to access crn`() {
    webTestClient.get().uri("/risks/crn/FORBIDDEN/predictors/all")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `should return not found when Delius cannot find crn`() {
    val response = webTestClient.get().uri("/risks/crn/USER_ACCESS_NOT_FOUND/predictors/all")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isNotFound
      .expectBody<ApiErrorResponse>()
      .returnResult().responseBody

    assertThat(response.developerMessage).isEqualTo("No such offender for CRN: USER_ACCESS_NOT_FOUND")
  }

  @Test
  fun `should return not found when Delius cannot find user`() {
    val response = webTestClient.get().uri("/risks/crn/X123456/predictors/all")
      .headers(setAuthorisation(user = "USER_NOT_FOUND", roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isNotFound
      .expectBody<ApiErrorResponse>()
      .returnResult().responseBody

    assertThat(response.developerMessage).isEqualTo("No such user for username: USER_NOT_FOUND")
  }
}
