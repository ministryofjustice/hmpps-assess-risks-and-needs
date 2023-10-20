package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.OffenderAssessmentApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.QuestionAnswerDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.SectionAnswersDto
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
@DisplayName("Risk Service Tests")
class RiskServiceTest {

  private val offenderAssessmentApiRestClient: OffenderAssessmentApiRestClient = mockk()
  private val auditService: AuditService = mockk()
  private val riskService = RiskService(offenderAssessmentApiRestClient, auditService)

  @BeforeEach
  fun setup() {
    every { auditService.sendEvent(any(), any()) } returns Unit
  }

  @Test
  fun `risk Summary contains highest overall Risk Level Custody Very High`() {
    val crn = "CRN123"
    val date = LocalDateTime.now()

    every {
      offenderAssessmentApiRestClient.getRoshSectionsForCompletedLastYearAssessment(crn)
    } returns SectionAnswersDto(
      assessmentId = 1,
      sections = mapOf(
        "ROSHSUM" to listOf(
          QuestionAnswerDto("SUM6.1.1", "", "L", "Low"),
          QuestionAnswerDto("SUM6.1.2", "", "L", "Low"),
          QuestionAnswerDto("SUM6.2.1", "", "M", "Medium"),
          QuestionAnswerDto("SUM6.2.2", "", "V", "Very High"),
          QuestionAnswerDto("SUM6.3.1", "", "L", "Low"),
          QuestionAnswerDto("SUM6.3.2", "", "L", "Low"),
        ),
      ),
      assessedOn = date,
    )

    val riskSummary = riskService.getRoshRiskSummaryByCrn(crn)
    verify(exactly = 1) { auditService.sendEvent(EventType.ACCESSED_ROSH_RISKS_SUMMARY, mapOf("crn" to crn)) }
    assertThat(riskSummary.overallRiskLevel).isEqualTo(RiskLevel.VERY_HIGH)
  }

  @Test
  fun `risk Summary contains highest overall Risk Level Community Medium`() {
    val crn = "CRN123"
    val date = LocalDateTime.now()

    every {
      offenderAssessmentApiRestClient.getRoshSectionsForCompletedLastYearAssessment(crn)
    } returns SectionAnswersDto(
      assessmentId = 1,
      sections = mapOf(
        "ROSHSUM" to listOf(
          QuestionAnswerDto("SUM6.1.1", "", "L", "Low"),
          QuestionAnswerDto("SUM6.1.2", "", "L", "Low"),
          QuestionAnswerDto("SUM6.2.1", "", "M", "Medium"),
          QuestionAnswerDto("SUM6.3.1", "", "L", "Low"),
          QuestionAnswerDto("SUM6.3.2", "", "L", "Low"),
        ),
      ),
      assessedOn = date,
    )

    val riskSummary = riskService.getRoshRiskSummaryByCrn(crn)
    assertThat(riskSummary.overallRiskLevel).isEqualTo(RiskLevel.MEDIUM)
  }

  @Test
  fun `overall risk level is null when risks returned`() {
    val crn = "CRN123"
    val date = LocalDateTime.now()

    every {
      offenderAssessmentApiRestClient.getRoshSectionsForCompletedLastYearAssessment(crn)
    } returns SectionAnswersDto(
      assessmentId = 1,
      sections = mapOf(
        "ROSHSUM" to listOf(),
      ),
      assessedOn = date,
    )
    val riskSummary = riskService.getRoshRiskSummaryByCrn(crn)
    assertThat(riskSummary.overallRiskLevel).isNull()
  }

  @Test
  fun `sends an audit event when getting fulltext RoSH risk`() {
    val crn = "CRN123"
    val date = LocalDateTime.now()

    every {
      offenderAssessmentApiRestClient.getRoshSectionsForCompletedLastYearAssessment(crn)
    } returns SectionAnswersDto(
      assessmentId = 1,
      sections = mapOf(
        "ROSHSUM" to emptyList(),
      ),
      assessedOn = date,
    )

    riskService.getFulltextRoshRisksByCrn(crn)
    verify(exactly = 1) { auditService.sendEvent(EventType.ACCESSED_ROSH_RISKS_FULLTEXT, mapOf("crn" to crn)) }
  }

  @Test
  fun `sends an audit event when getting RoSH risk summary`() {
    val crn = "CRN123"
    val date = LocalDateTime.now()

    every {
      offenderAssessmentApiRestClient.getRoshSectionsForCompletedLastYearAssessment(crn)
    } returns SectionAnswersDto(
      assessmentId = 1,
      sections = mapOf(
        "ROSHSUM" to emptyList(),
      ),
      assessedOn = date,
    )

    riskService.getRoshRiskSummaryByCrn(crn)
    verify(exactly = 1) { auditService.sendEvent(EventType.ACCESSED_ROSH_RISKS_SUMMARY, mapOf("crn" to crn)) }
  }
}
