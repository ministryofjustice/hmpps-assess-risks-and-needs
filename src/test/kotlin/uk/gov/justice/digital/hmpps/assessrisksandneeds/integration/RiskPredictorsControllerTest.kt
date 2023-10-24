package uk.gov.justice.digital.hmpps.assessrisksandneeds.integration

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.http.HttpStatus
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlConfig
import org.springframework.test.context.jdbc.SqlGroup
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
@SqlGroup(
  Sql(
    scripts = ["classpath:rsrPredictorHistory/before-test.sql"],
    config = SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED),
  ),
  Sql(
    scripts = ["classpath:rsrPredictorHistory/after-test.sql"],
    config = SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED),
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
  ),
)
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
      assertThat(rsrPercentageScore).isEqualTo(BigDecimal.valueOf(40.44))
      assertThat(rsrScoreLevel).isEqualTo(ScoreLevel.HIGH)
      assertThat(calculatedDate).isEqualTo(LocalDateTime.of(2021, 10, 5, 9, 6))
      assertThat(completedDate).isEqualTo(LocalDateTime.of(2021, 9, 14, 9, 7))
      assertThat(staticOrDynamic).isEqualTo(ScoreType.DYNAMIC)
      assertThat(source).isEqualTo(RsrScoreSource.ASSESSMENTS_API)
      assertThat(status).isEqualTo(AssessmentStatus.COMPLETE)
      assertThat(algorithmVersion).isEqualTo("3")
    }
    with(rsrHistory[1]) {
      assertThat(rsrPercentageScore).isEqualTo(BigDecimal.valueOf(84.36))
      assertThat(rsrScoreLevel).isEqualTo(ScoreLevel.HIGH)
      assertThat(calculatedDate).isNull()
      assertThat(completedDate).isEqualTo(LocalDateTime.of(2021, 6, 21, 15, 55, 4))
      assertThat(staticOrDynamic).isEqualTo(ScoreType.DYNAMIC)
      assertThat(source).isEqualTo(RsrScoreSource.OASYS)
      assertThat(status).isEqualTo(AssessmentStatus.COMPLETE)
      assertThat(algorithmVersion).isEqualTo("3")
    }
    with(rsrHistory[2]) {
      assertThat(rsrPercentageScore).isEqualTo(BigDecimal.valueOf(20.22))
      assertThat(rsrScoreLevel).isEqualTo(ScoreLevel.LOW)
      assertThat(ospcPercentageScore).isEqualTo(BigDecimal.valueOf(10.1))
      assertThat(ospcScoreLevel).isEqualTo(ScoreLevel.MEDIUM)
      assertThat(ospiPercentageScore).isEqualTo(BigDecimal.valueOf(30.3))
      assertThat(ospiScoreLevel).isEqualTo(ScoreLevel.HIGH)
      assertThat(calculatedDate).isEqualTo(LocalDateTime.of(2019, 11, 14, 9, 6))
      assertThat(completedDate).isEqualTo(LocalDateTime.of(2019, 11, 14, 9, 7))
      assertThat(staticOrDynamic).isEqualTo(ScoreType.STATIC)
      assertThat(source).isEqualTo(RsrScoreSource.ASSESSMENTS_API)
      assertThat(status).isEqualTo(AssessmentStatus.COMPLETE)
      assertThat(algorithmVersion).isEqualTo("3")
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
