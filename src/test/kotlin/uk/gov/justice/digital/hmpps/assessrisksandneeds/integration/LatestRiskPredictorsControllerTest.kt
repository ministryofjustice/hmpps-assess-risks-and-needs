package uk.gov.justice.digital.hmpps.assessrisksandneeds.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OgpScoreDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OgrScoreDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OvpScoreDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskScoresDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrScoreDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrScoreSource
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreType
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
      .headers(setAuthorisation(user = "Gary C", roles = listOf("ROLE_PROBATION")))
      .exchange()
      // Then
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody<List<RiskScoresDto>>()
      .consumeWith {
        assertThat(it.responseBody)
          .hasSize(1)
        assertThat(it.responseBody[0]).usingRecursiveComparison()
          .isEqualTo(
            RiskScoresDto(
              completedDate = LocalDateTime.of(2022, 5, 27, 0, 0, 0),
              groupReconvictionScore = OgrScoreDto(
                oneYear = BigDecimal.valueOf(3),
                twoYears = BigDecimal.valueOf(5),
                scoreLevel = ScoreLevel.LOW
              ),
              violencePredictorScore = OvpScoreDto(
                ovpStaticWeightedScore = BigDecimal.valueOf(14),
                ovpDynamicWeightedScore = BigDecimal.valueOf(3),
                ovpTotalWeightedScore = BigDecimal.valueOf(17),
                oneYear = BigDecimal.valueOf(4),
                twoYears = BigDecimal.valueOf(7),
                ovpRisk = ScoreLevel.LOW
              ),
              generalPredictorScore = OgpScoreDto(
                ogpStaticWeightedScore = BigDecimal.valueOf(3),
                ogpDynamicWeightedScore = BigDecimal.valueOf(7),
                ogpTotalWeightedScore = BigDecimal.valueOf(10),
                ogp1Year = BigDecimal.valueOf(4),
                ogp2Year = BigDecimal.valueOf(8),
                ogpRisk = ScoreLevel.LOW
              ),
              riskOfSeriousRecidivismScore = RsrScoreDto(
                percentageScore = BigDecimal.valueOf(50.1234),
                staticOrDynamic = ScoreType.STATIC,
                source = RsrScoreSource.OASYS,
                algorithmVersion = "11",
                ScoreLevel.LOW
              )
            )
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
}
