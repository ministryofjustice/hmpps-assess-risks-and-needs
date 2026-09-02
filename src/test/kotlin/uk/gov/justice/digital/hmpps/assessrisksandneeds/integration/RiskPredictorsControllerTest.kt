package uk.gov.justice.digital.hmpps.assessrisksandneeds.integration

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentStatus
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrPredictorVersioned
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrPredictorVersionedDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrPredictorVersionedLegacyDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrScoreSource
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.AuditService
import java.math.BigDecimal
import java.time.LocalDateTime

@AutoConfigureWebTestClient(timeout = "360000000")
@DisplayName("Risk Predictors Tests")
class RiskPredictorsControllerTest : IntegrationTestBase() {

  @MockkBean
  private lateinit var auditService: AuditService

  @BeforeEach
  fun setup() {
    every { auditService.sendEvent(any(), any()) } returns Unit
  }

  @Test
  fun `get all rsr scores should convert identifier type regardless of case`() {
    val identifierType = "cRn"
    val identifierValue = "X234567"

    webTestClient.get().uri("/risks/predictors/rsr/$identifierType/$identifierValue")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isOk
  }

  @Test
  fun `get all rsr scores for a crn identifier type`() {
    val identifierType = "crn"
    val identifierValue = "X123456"

    val rsrScores = webTestClient.get()
      .uri("/risks/predictors/rsr/$identifierType/$identifierValue")
      .header("Content-Type", "application/json")
      .headers(setAuthorisation(user = "assess-risks-needs", roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody<List<RsrPredictorVersioned<Any>>>()
      .returnResult().responseBody

    assertThat(rsrScores).hasSize(6)
    assertThat(rsrScores[0].outputVersion).isEqualTo("2")
    val standaloneRsrScore = rsrScores[0] as RsrPredictorVersionedDto
    with(standaloneRsrScore) {
      assertThat(completedDate).isEqualTo(LocalDateTime.of(2026, 7, 27, 15, 40, 41))
      assertThat(source).isEqualTo(RsrScoreSource.OASYS)
      assertThat(status).isEqualTo(AssessmentStatus.COMPLETE)
      assertThat(output?.combinedSeriousReoffendingPredictor?.score).isEqualTo(BigDecimal.valueOf(1.79))
      assertThat(output?.combinedSeriousReoffendingPredictor?.band).isEqualTo(ScoreLevel.MEDIUM)
      assertThat(output?.combinedSeriousReoffendingPredictor?.staticOrDynamic).isEqualTo(ScoreType.STATIC)
    }
    assertThat(rsrScores[1].outputVersion).isEqualTo("2")
    val secondVersionedRsrScore = rsrScores[1] as RsrPredictorVersionedDto
    with(secondVersionedRsrScore) {
      assertThat(completedDate).isEqualTo(LocalDateTime.of(2022, 6, 12, 18, 23, 20))
      assertThat(source).isEqualTo(RsrScoreSource.OASYS)
      assertThat(status).isEqualTo(AssessmentStatus.COMPLETE)
      assertThat(output?.combinedSeriousReoffendingPredictor?.score).isEqualTo(BigDecimal.valueOf(1.23))
      assertThat(output?.combinedSeriousReoffendingPredictor?.band).isEqualTo(ScoreLevel.LOW)
      assertThat(output?.combinedSeriousReoffendingPredictor?.staticOrDynamic).isEqualTo(ScoreType.STATIC)
    }
    assertThat(rsrScores[3].outputVersion).isEqualTo("1")
    val fourthLegacyRsrScore = rsrScores[3] as RsrPredictorVersionedLegacyDto
    with(fourthLegacyRsrScore) {
      assertThat(completedDate).isEqualTo(LocalDateTime.of(2022, 6, 10, 18, 23, 20))
      assertThat(source).isEqualTo(RsrScoreSource.OASYS)
      assertThat(status).isEqualTo(AssessmentStatus.COMPLETE)
      assertThat(output?.rsrPercentageScore).isEqualTo(BigDecimal.valueOf(50.1234))
      assertThat(output?.rsrScoreLevel).isEqualTo(ScoreLevel.MEDIUM)
      assertThat(output?.staticOrDynamic).isEqualTo(ScoreType.DYNAMIC)
    }
    assertThat(rsrScores[5].outputVersion).isEqualTo("1")
    val sixthLegacyRsrScore = rsrScores[5] as RsrPredictorVersionedLegacyDto
    with(sixthLegacyRsrScore) {
      assertThat(completedDate).isEqualTo(LocalDateTime.of(2022, 4, 27, 12, 46, 39))
      assertThat(source).isEqualTo(RsrScoreSource.OASYS)
      assertThat(status).isEqualTo(AssessmentStatus.COMPLETE)
      assertThat(output?.rsrPercentageScore).isEqualTo(BigDecimal.valueOf(0.32))
      assertThat(output?.rsrScoreLevel).isEqualTo(ScoreLevel.LOW)
      assertThat(output?.staticOrDynamic).isEqualTo(ScoreType.STATIC)
    }
  }

  @Test
  fun `get all rsr scores for a crn identifier type when no rsr returned from assessment API`() {
    val identifierType = "CRN"
    val identifierValue = "X234567"

    val rsrScores = webTestClient.get()
      .uri("/risks/predictors/rsr/$identifierType/$identifierValue")
      .header("Content-Type", "application/json")
      .headers(setAuthorisation(user = "assess-risks-needs", roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody<List<RsrPredictorVersioned<Any>>>()
      .returnResult().responseBody

    assertThat(rsrScores).isEmpty()
  }

  @Test
  fun `get all rsr scores should return bad request for invalid identifier type`() {
    val identifierType = "INVALID_IDENTIFIER_TYPE"
    val identifierValue = "X234567"

    webTestClient.get().uri("/risks/predictors/rsr/$identifierType/$identifierValue")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isBadRequest
  }
}
