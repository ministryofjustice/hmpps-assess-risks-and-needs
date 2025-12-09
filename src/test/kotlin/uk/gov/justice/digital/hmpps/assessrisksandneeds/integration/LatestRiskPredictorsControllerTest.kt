package uk.gov.justice.digital.hmpps.assessrisksandneeds.integration

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AllPredictorVersioned
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AllPredictorVersionedDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AllPredictorVersionedLegacyDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentStatus
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OgpScoreDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OgrScoreDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OspScoreDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OvpScoreDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskScoresDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrScoreDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrScoreSource
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ogrs4.AllPredictorDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ogrs4.BasePredictorDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ogrs4.StaticOrDynamicPredictorDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ogrs4.VersionedStaticOrDynamicPredictorDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.ApiErrorResponse
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.AuditService
import java.math.BigDecimal
import java.time.LocalDateTime

@AutoConfigureWebTestClient(timeout = "360000000")
class LatestRiskPredictorsControllerTest : IntegrationTestBase() {

  @MockkBean
  private lateinit var auditService: AuditService

  @BeforeEach
  fun setup() {
    every { auditService.sendEvent(any(), any()) } returns Unit
  }

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
        assertThat(it.responseBody).hasSize(5)
        assertThat(it.responseBody!![0]).usingRecursiveComparison()
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
                algorithmVersion = "5",
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

    assertThat(response!!.developerMessage).isEqualTo("No such offender for CRN: USER_ACCESS_NOT_FOUND")
  }

  @Test
  fun `should return versioned risk data for valid crn`() {
    // Given
    val identifierType = "crn"
    val identifierValue = "X123456"

    // When
    webTestClient.get()
      .uri("/risks/predictors/all/$identifierType/$identifierValue")
      .header("Content-Type", "application/json")
      .headers(setAuthorisation(user = "assess-risks-needs", roles = listOf("ROLE_PROBATION")))
      .exchange()
      // Then
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody<List<AllPredictorVersioned<Any>>>()
      .consumeWith {
        assertThat(it.responseBody).hasSize(5)
        assertThat(it.responseBody!![0]).usingRecursiveComparison()
          .isEqualTo(
            AllPredictorVersionedLegacyDto(
              completedDate = LocalDateTime.of(2022, 6, 10, 18, 23, 20),
              status = AssessmentStatus.COMPLETE,
              outputVersion = "1",
              output = RiskScoresDto(
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
                  algorithmVersion = "5",
                  ScoreLevel.MEDIUM,
                ),
                sexualPredictorScore = OspScoreDto(
                  ospIndecentPercentageScore = BigDecimal.valueOf(2.81),
                  ospContactPercentageScore = BigDecimal.valueOf(1.07),
                  ospIndecentScoreLevel = ScoreLevel.MEDIUM,
                  ospContactScoreLevel = ScoreLevel.MEDIUM,
                ),
              ),
            ),
          )
        assertThat(it.responseBody!![4]).usingRecursiveComparison()
          .isEqualTo(
            AllPredictorVersionedDto(
              completedDate = LocalDateTime.of(2022, 6, 12, 18, 23, 20),
              status = AssessmentStatus.COMPLETE,
              outputVersion = "2",
              output = AllPredictorDto(
                allReoffendingPredictor = StaticOrDynamicPredictorDto(
                  staticOrDynamic = ScoreType.STATIC,
                  score = BigDecimal.valueOf(1.23),
                  band = ScoreLevel.LOW,
                ),
                violentReoffendingPredictor = StaticOrDynamicPredictorDto(
                  staticOrDynamic = ScoreType.STATIC,
                  score = BigDecimal.valueOf(1.23),
                  band = ScoreLevel.LOW,
                ),
                seriousViolentReoffendingPredictor = StaticOrDynamicPredictorDto(
                  staticOrDynamic = ScoreType.STATIC,
                  score = BigDecimal.valueOf(1.23),
                  band = ScoreLevel.LOW,
                ),
                directContactSexualReoffendingPredictor = BasePredictorDto(
                  score = BigDecimal.valueOf(2.81),
                  band = ScoreLevel.MEDIUM,
                ),
                indirectImageContactSexualReoffendingPredictor = BasePredictorDto(
                  score = BigDecimal.valueOf(1.07),
                  band = ScoreLevel.MEDIUM,
                ),
                combinedSeriousReoffendingPredictor = VersionedStaticOrDynamicPredictorDto(
                  algorithmVersion = "6",
                  staticOrDynamic = ScoreType.STATIC,
                  score = BigDecimal.valueOf(1.23),
                  band = ScoreLevel.LOW,
                ),
              ),
            ),
          )
      }
  }

  @Test
  fun `should return not found error for invalid crn for versioned risk scores`() {
    webTestClient.get().uri("/risks/predictors/all/crn/X999999")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `should return 400 bad request for invalid identifier type for versioned risk scores`() {
    val identifierType = "INVALID_IDENTIFIER_TYPE"
    val identifierValue = "X234567"
    webTestClient.get().uri("/risks/predictors/all/$identifierType/$identifierValue")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isBadRequest
  }

  @Test
  fun `should return forbidden when user has insufficient privileges to access crn for versioned risk scores`() {
    webTestClient.get().uri("/risks/predictors/all/crn/FORBIDDEN")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `should return not found when Delius cannot find crn for versioned risk scores`() {
    val response = webTestClient.get().uri("/risks/predictors/all/crn/USER_ACCESS_NOT_FOUND")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isNotFound
      .expectBody<ApiErrorResponse>()
      .returnResult().responseBody

    assertThat(response!!.developerMessage).isEqualTo("No such offender for CRN: USER_ACCESS_NOT_FOUND")
  }

  @Test
  fun `should return legacy risk data for legacy assessment ID`() {
    // Given
    val id = "1000001"

    // When
    webTestClient.get()
      .uri("/assessments/id/$id/risk/predictors/all")
      .header("Content-Type", "application/json")
      .headers(setAuthorisation(user = "assess-risks-needs", roles = listOf("ROLE_ARNS__RISKS__RO")))
      .exchange()
      // Then
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody<AllPredictorVersioned<Any>>()
      .value {
        assertThat(it).usingRecursiveComparison()
          .isEqualTo(
            AllPredictorVersionedLegacyDto(
              outputVersion = "1",
              output = RiskScoresDto(
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
                  algorithmVersion = "5",
                  ScoreLevel.MEDIUM,
                ),
                sexualPredictorScore = OspScoreDto(
                  ospIndecentPercentageScore = BigDecimal.valueOf(2.81),
                  ospContactPercentageScore = BigDecimal.valueOf(1.07),
                  ospIndecentScoreLevel = ScoreLevel.MEDIUM,
                  ospContactScoreLevel = ScoreLevel.MEDIUM,
                ),
              ),
            ),
          )
      }
  }

  @Test
  fun `should return OGRS4 risk data for OGRS4 assessment ID`() {
    // Given
    val id = "1000002"

    // When
    webTestClient.get()
      .uri("/assessments/id/$id/risk/predictors/all")
      .header("Content-Type", "application/json")
      .headers(setAuthorisation(user = "assess-risks-needs", roles = listOf("ROLE_ARNS__RISKS__RO")))
      .exchange()
      // Then
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody<AllPredictorVersioned<Any>>()
      .value {
        assertThat(it).usingRecursiveComparison()
          .isEqualTo(
            AllPredictorVersionedDto(
              outputVersion = "2",
              output = AllPredictorDto(
                allReoffendingPredictor = StaticOrDynamicPredictorDto(
                  staticOrDynamic = ScoreType.STATIC,
                  score = BigDecimal("1.23"),
                  band = ScoreLevel.LOW,
                ),
                violentReoffendingPredictor = StaticOrDynamicPredictorDto(
                  staticOrDynamic = ScoreType.STATIC,
                  score = BigDecimal("1.23"),
                  band = ScoreLevel.LOW,
                ),
                seriousViolentReoffendingPredictor = StaticOrDynamicPredictorDto(
                  staticOrDynamic = ScoreType.STATIC,
                  score = BigDecimal("1.23"),
                  band = ScoreLevel.LOW,
                ),
                directContactSexualReoffendingPredictor = BasePredictorDto(
                  score = BigDecimal("2.81"),
                  band = ScoreLevel.MEDIUM,
                ),
                indirectImageContactSexualReoffendingPredictor = BasePredictorDto(
                  score = BigDecimal("1.07"),
                  band = ScoreLevel.MEDIUM,
                ),
                combinedSeriousReoffendingPredictor = VersionedStaticOrDynamicPredictorDto(
                  algorithmVersion = "6",
                  staticOrDynamic = ScoreType.STATIC,
                  score = BigDecimal("1.23"),
                  band = ScoreLevel.LOW,
                ),
              ),
            ),
          )
      }
  }

  @Test
  fun `should return 404 when risk data cannot be found for assessment ID`() {
    // Given
    val id = "1000003"

    // When
    webTestClient.get()
      .uri("/assessments/id/$id/risk/predictors/all")
      .header("Content-Type", "application/json")
      .headers(setAuthorisation(user = "assess-risks-needs", roles = listOf("ROLE_ARNS__RISKS__RO")))
      .exchange()
      // Then
      .expectStatus().isNotFound
  }
}
