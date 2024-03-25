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
