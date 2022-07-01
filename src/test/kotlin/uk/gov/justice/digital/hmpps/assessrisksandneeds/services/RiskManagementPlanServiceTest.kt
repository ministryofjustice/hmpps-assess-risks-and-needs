package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskManagementPlanDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskManagementPlansDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.TimelineDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.AssessmentApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysRiskManagementPlanDetailsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysRiskManagementPlanDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.EntityNotFoundException
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
class RiskManagementPlanServiceTest {

  @MockK
  private val assessmentClient: AssessmentApiRestClient = mockk()
  private val riskManagementPlanService = RiskManagementPlanService(assessmentClient)

  @Test
  fun `get latest risk management plan for a given CRN from offender-assessments-api`() {
    val crn = "X12345"
    val dateCompleted = LocalDateTime.of(2022, 1, 7, 12, 0)
    val initiationDate = LocalDateTime.of(2022, 1, 3, 12, 0)
    val oasysRiskManagementPlans = OasysRiskManagementPlanDetailsDto(
      crn = crn,
      riskManagementPlans = listOf(
        OasysRiskManagementPlanDto(
          assessmentPk = 3,
          dateCompleted = dateCompleted,
          initiationDate = initiationDate,
          assessmentStatus = "COMPLETE",
          keyConsiderationsCurrentSituation = "patternOfOffending",
          furtherConsiderationsCurrentSituation = "",
          supervision = "",
          monitoringAndControl = "",
          interventionsAndTreatment = "",
          victimSafetyPlanning = "",
          contingencyPlans = ""
        )
      ),
      timeline = listOf(
        TimelineDto(
          assessmentId = 1,
          initiationDate = LocalDateTime.of(2022, 1, 1, 12, 0),
          status = "LOCKED_INCOMPLETE",
          completedDate = LocalDateTime.of(2022, 1, 5, 12, 0),
        ),
        TimelineDto(
          assessmentId = 2,
          initiationDate = LocalDateTime.of(2022, 1, 2, 12, 0),
          status = "SIGNED",
          completedDate = null,
        ),
        TimelineDto(
          assessmentId = 3,
          initiationDate = initiationDate,
          status = "COMPLETE",
          completedDate = dateCompleted,
        )
      )
    )
    every { assessmentClient.getRiskManagementPlan(any(), any()) }.returns(oasysRiskManagementPlans)

    val result = riskManagementPlanService.getRiskManagementPlans(crn)

    verify(exactly = 1) { assessmentClient.getRiskManagementPlan(crn, "LIMIT") }
    assertThat(result)
      .isEqualTo(
        RiskManagementPlansDto(
          crn = crn,
          riskManagementPlan = listOf(
            RiskManagementPlanDto(
              assessmentId = 1,
              dateCompleted = LocalDateTime.of(2022, 1, 5, 12, 0),
              initiationDate = LocalDateTime.of(2022, 1, 1, 12, 0),
              assessmentStatus = "LOCKED_INCOMPLETE",
            ),
            RiskManagementPlanDto(
              assessmentId = 2,
              dateCompleted = null,
              initiationDate = LocalDateTime.of(2022, 1, 2, 12, 0),
              assessmentStatus = "SIGNED",
            ),
            RiskManagementPlanDto(
              assessmentId = 3,
              dateCompleted = dateCompleted,
              initiationDate = initiationDate,
              assessmentStatus = "COMPLETE",
              keyConsiderationsCurrentSituation = "patternOfOffending",
              furtherConsiderationsCurrentSituation = "",
              supervision = "",
              monitoringAndControl = "",
              interventionsAndTreatment = "",
              victimSafetyPlanning = "",
              contingencyPlans = ""
            )
          )
        )
      )
  }

  @Test
  fun `get latest risk management plan and assessment summaries for a given CRN when timeline including multiple completed assessments`() {
    val crn = "X12345"
    val dateCompleted = LocalDateTime.of(2022, 1, 7, 12, 0)
    val initiationDate = LocalDateTime.of(2022, 1, 3, 12, 0)
    val oasysRiskManagementPlans = OasysRiskManagementPlanDetailsDto(
      crn = crn,
      riskManagementPlans = listOf(
        OasysRiskManagementPlanDto(
          assessmentPk = 3,
          dateCompleted = dateCompleted,
          initiationDate = initiationDate,
          assessmentStatus = "COMPLETE",
          keyConsiderationsCurrentSituation = "patternOfOffending",
          furtherConsiderationsCurrentSituation = "",
          supervision = "",
          monitoringAndControl = "",
          interventionsAndTreatment = "",
          victimSafetyPlanning = "",
          contingencyPlans = ""
        )
      ),
      timeline = listOf(
        TimelineDto(
          assessmentId = 1,
          initiationDate = LocalDateTime.of(2022, 1, 1, 12, 0),
          status = "LOCKED_INCOMPLETE",
          completedDate = LocalDateTime.of(2022, 1, 5, 12, 0),
        ),
        TimelineDto(
          assessmentId = 2,
          initiationDate = LocalDateTime.of(2022, 1, 2, 12, 0),
          status = "SIGNED",
          completedDate = null,
        ),
        TimelineDto(
          assessmentId = 3,
          initiationDate = initiationDate,
          status = "COMPLETE",
          completedDate = dateCompleted,
        ),
        TimelineDto(
          assessmentId = 4,
          initiationDate = initiationDate.plusDays(1),
          status = "COMPLETE",
          completedDate = dateCompleted.plusDays(1),
        )
      )
    )
    every { assessmentClient.getRiskManagementPlan(any(), any()) }.returns(oasysRiskManagementPlans)

    val result = riskManagementPlanService.getRiskManagementPlans(crn)

    verify(exactly = 1) { assessmentClient.getRiskManagementPlan(crn, "LIMIT") }
    assertThat(result)
      .isEqualTo(
        RiskManagementPlansDto(
          crn = crn,
          riskManagementPlan = listOf(
            RiskManagementPlanDto(
              assessmentId = 1,
              dateCompleted = LocalDateTime.of(2022, 1, 5, 12, 0),
              initiationDate = LocalDateTime.of(2022, 1, 1, 12, 0),
              assessmentStatus = "LOCKED_INCOMPLETE",
            ),
            RiskManagementPlanDto(
              assessmentId = 2,
              dateCompleted = null,
              initiationDate = LocalDateTime.of(2022, 1, 2, 12, 0),
              assessmentStatus = "SIGNED",
            ),
            RiskManagementPlanDto(
              assessmentId = 3,
              dateCompleted = dateCompleted,
              initiationDate = initiationDate,
              assessmentStatus = "COMPLETE",
              keyConsiderationsCurrentSituation = "patternOfOffending",
              furtherConsiderationsCurrentSituation = "",
              supervision = "",
              monitoringAndControl = "",
              interventionsAndTreatment = "",
              victimSafetyPlanning = "",
              contingencyPlans = ""
            ),
            RiskManagementPlanDto(
              assessmentId = 4,
              dateCompleted = dateCompleted.plusDays(1),
              initiationDate = initiationDate.plusDays(1),
              assessmentStatus = "COMPLETE",
            )
          )
        )
      )
  }

  @Test
  fun `should throw exception when crn is not found for given CRN`() {
    // Given
    val crn = "DOES_NOT_EXIST"
    every {
      assessmentClient.getRiskManagementPlan(
        any(),
        any()
      )
    }.throws(EntityNotFoundException("No matching offender"))

    // When
    assertThrows<EntityNotFoundException> {
      riskManagementPlanService.getRiskManagementPlans(crn)
    }
  }

  @Test
  fun `should throw exception when completed risk management plan is not found for given CRN`() {
    // Given
    val crn = "NO_RISK_MANAGEMENT_PLAN"
    every {
      assessmentClient.getRiskManagementPlan(
        any(),
        any()
      )
    }.throws(EntityNotFoundException("No matching completed risk management plan found for this offender"))

    // When
    assertThrows<EntityNotFoundException> {
      riskManagementPlanService.getRiskManagementPlans(crn)
    }
  }
}
