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
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrPredictorDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrPredictorVersioned
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

    assertThat(rsrScores).hasSize(3)
    assertThat(rsrScores[0].version).isEqualTo(1)
    val firstLegacyRsrScore = rsrScores[0] as RsrPredictorVersionedLegacyDto
    with(firstLegacyRsrScore) {
      assertThat(completedDate).isEqualTo(LocalDateTime.of(2022, 6, 10, 18, 23, 20))
      assertThat(source).isEqualTo(RsrScoreSource.OASYS)
      assertThat(status).isEqualTo(AssessmentStatus.COMPLETE)
      assertThat(output?.rsrPercentageScore).isEqualTo(BigDecimal.valueOf(50.1234))
      assertThat(output?.rsrScoreLevel).isEqualTo(ScoreLevel.MEDIUM)
      assertThat(output?.staticOrDynamic).isEqualTo(ScoreType.DYNAMIC)
    }

    assertThat(rsrScores[2].version).isEqualTo(1)
    val thirdLegacyRsrScore = rsrScores[2] as RsrPredictorVersionedLegacyDto
    with(thirdLegacyRsrScore) {
      assertThat(calculatedDate).isNull()
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

  @Test
  fun `get all rsr score history for a crn`() {
    val crn = "X123456"
    val rsrHistory = webTestClient.get()
      .uri("/risks/crn/$crn/predictors/rsr/history")
      .header("Content-Type", "application/json")
      .headers(setAuthorisation(user = "assess-risks-needs", roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody<List<RsrPredictorDto>>()
      .returnResult().responseBody

    assertThat(rsrHistory).hasSize(3)
    with(rsrHistory[0]) {
      assertThat(rsrPercentageScore).isEqualTo(BigDecimal.valueOf(50.1234))
      assertThat(rsrScoreLevel).isEqualTo(ScoreLevel.MEDIUM)
      assertThat(completedDate).isEqualTo(LocalDateTime.of(2022, 6, 10, 18, 23, 20))
      assertThat(staticOrDynamic).isEqualTo(ScoreType.DYNAMIC)
      assertThat(source).isEqualTo(RsrScoreSource.OASYS)
      assertThat(status).isEqualTo(AssessmentStatus.COMPLETE)
    }
    with(rsrHistory[2]) {
      assertThat(rsrPercentageScore).isEqualTo(BigDecimal.valueOf(0.32))
      assertThat(rsrScoreLevel).isEqualTo(ScoreLevel.LOW)
      assertThat(calculatedDate).isNull()
      assertThat(completedDate).isEqualTo(LocalDateTime.of(2022, 4, 27, 12, 46, 39))
      assertThat(staticOrDynamic).isEqualTo(ScoreType.STATIC)
      assertThat(source).isEqualTo(RsrScoreSource.OASYS)
      assertThat(status).isEqualTo(AssessmentStatus.COMPLETE)
    }
  }

  @Test
  fun `get all rsr score history for a crn when no rsr returned from assessment API`() {
    val crn = "X234567"
    val rsrHistory = webTestClient.get()
      .uri("/risks/crn/$crn/predictors/rsr/history")
      .header("Content-Type", "application/json")
      .headers(setAuthorisation(user = "assess-risks-needs", roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody<List<RsrPredictorDto>>()
      .returnResult().responseBody
    assertThat(rsrHistory).isEmpty()
  }
}
