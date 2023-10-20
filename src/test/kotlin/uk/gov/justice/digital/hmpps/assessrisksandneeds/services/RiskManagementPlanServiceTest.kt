package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.aspectj.weaver.tools.cache.SimpleCacheFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.MDC
import org.springframework.http.HttpMethod
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.CaseAccess
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskManagementPlanDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskManagementPlansDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.CommunityApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.ExternalService
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.OasysApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysRiskManagementPlanDetailsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysRiskManagementPlanDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.TimelineDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.EntityNotFoundException
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.ExternalApiForbiddenException
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
class RiskManagementPlanServiceTest {

  private val oasysApiRestClient: OasysApiRestClient = mockk()
  private val communityClient: CommunityApiRestClient = mockk()
  private val auditService: AuditService = mockk()
  private val riskManagementPlanService = RiskManagementPlanService(oasysApiRestClient, communityClient, auditService)

  @BeforeEach
  fun setup() {
    every { communityClient.verifyUserAccess(any(), any()) } answers {
      CaseAccess(
        it.invocation.args[0] as String,
        userExcluded = false,
        userRestricted = false,
        null,
        null,
      )
    }
    mockkStatic(MDC::class)
    every { MDC.get(any()) } returns "userName"
    every { auditService.sendEvent(any(), any()) } returns Unit
  }

  @Test
  fun `get latest risk management plan for a given CRN from offender-assessments-api`() {
    val crn = "X12345"
    val dateCompleted = LocalDateTime.of(2022, 1, 7, 12, 0)
    val initiationDate = LocalDateTime.of(2022, 1, 3, 12, 0)
    val oasysRiskManagementPlans = OasysRiskManagementPlanDetailsDto(
      crn = crn,
      limitedAccessOffender = false,
      riskManagementPlans = listOf(
        OasysRiskManagementPlanDto(
          assessmentPk = 3,
          assessmentType = "LAYER1",
          dateCompleted = dateCompleted,
          initiationDate = initiationDate,
          assessmentStatus = "COMPLETE",
          keyInformationCurrentSituation = "patternOfOffending",
          furtherConsiderationsCurrentSituation = "",
          supervision = "",
          monitoringAndControl = "",
          interventionsAndTreatment = "",
          victimSafetyPlanning = "",
          contingencyPlans = "",
          laterWIPAssessmentExists = false,
          latestWIPDate = null,
        ),
      ),
      timeline = listOf(
        TimelineDto(
          assessmentPk = 1,
          assessmentType = "LAYER1",
          initiationDate = LocalDateTime.of(2022, 1, 1, 12, 0),
          status = "LOCKED_INCOMPLETE",
          completedDate = LocalDateTime.of(2022, 1, 5, 12, 0),
        ),
        TimelineDto(
          assessmentPk = 2,
          assessmentType = "LAYER1",
          initiationDate = LocalDateTime.of(2022, 1, 2, 12, 0),
          status = "SIGNED",
          completedDate = null,
        ),
        TimelineDto(
          assessmentPk = 3,
          assessmentType = "LAYER1",
          initiationDate = initiationDate,
          status = "COMPLETE",
          completedDate = dateCompleted,
        ),
      ),
    )
    every { oasysApiRestClient.getRiskManagementPlan(any(), any()) }.returns(oasysRiskManagementPlans)

    val result = riskManagementPlanService.getRiskManagementPlans(crn)

    verify(exactly = 1) { auditService.sendEvent(EventType.ACCESSED_RISK_MANAGEMENT_PLAN, mapOf("crn" to crn)) }
    verify(exactly = 1) { oasysApiRestClient.getRiskManagementPlan(crn, "ALLOW") }
    assertThat(result)
      .isEqualTo(
        RiskManagementPlansDto(
          crn = crn,
          limitedAccessOffender = false,
          riskManagementPlan = listOf(
            RiskManagementPlanDto(
              assessmentId = 1,
              assessmentType = "LAYER1",
              dateCompleted = LocalDateTime.of(2022, 1, 5, 12, 0),
              initiationDate = LocalDateTime.of(2022, 1, 1, 12, 0),
              assessmentStatus = "LOCKED_INCOMPLETE",
            ),
            RiskManagementPlanDto(
              assessmentId = 2,
              assessmentType = "LAYER1",
              dateCompleted = null,
              initiationDate = LocalDateTime.of(2022, 1, 2, 12, 0),
              assessmentStatus = "SIGNED",
            ),
            RiskManagementPlanDto(
              assessmentId = 3,
              assessmentType = "LAYER1",
              dateCompleted = dateCompleted,
              initiationDate = initiationDate,
              assessmentStatus = "COMPLETE",
              keyInformationCurrentSituation = "patternOfOffending",
              furtherConsiderationsCurrentSituation = "",
              supervision = "",
              monitoringAndControl = "",
              interventionsAndTreatment = "",
              victimSafetyPlanning = "",
              contingencyPlans = "",
              laterWIPAssessmentExists = false,
              latestWIPDate = null,
            ),
          ),
        ),
      )
  }

  @Test
  fun `get latest risk management plan and assessment summaries for a given CRN when timeline including multiple completed assessments`() {
    val crn = "X12345"
    val dateCompleted = LocalDateTime.of(2022, 1, 7, 12, 0)
    val initiationDate = LocalDateTime.of(2022, 1, 3, 12, 0)
    val oasysRiskManagementPlans = OasysRiskManagementPlanDetailsDto(
      crn = crn,
      limitedAccessOffender = false,
      riskManagementPlans = listOf(
        OasysRiskManagementPlanDto(
          assessmentPk = 3,
          assessmentType = "LAYER1",
          dateCompleted = dateCompleted,
          initiationDate = initiationDate,
          assessmentStatus = "COMPLETE",
          keyInformationCurrentSituation = "patternOfOffending",
          furtherConsiderationsCurrentSituation = "",
          supervision = "",
          monitoringAndControl = "",
          interventionsAndTreatment = "",
          victimSafetyPlanning = "",
          contingencyPlans = "",
          laterWIPAssessmentExists = false,
          latestWIPDate = null,
        ),
      ),
      timeline = listOf(
        TimelineDto(
          assessmentPk = 1,
          assessmentType = "LAYER1",
          initiationDate = LocalDateTime.of(2022, 1, 1, 12, 0),
          status = "LOCKED_INCOMPLETE",
          completedDate = LocalDateTime.of(2022, 1, 5, 12, 0),
          partcompStatus = "Signed",
        ),
        TimelineDto(
          assessmentPk = 2,
          assessmentType = "LAYER1",
          initiationDate = LocalDateTime.of(2022, 1, 2, 12, 0),
          status = "SIGNED",
          completedDate = null,
        ),
        TimelineDto(
          assessmentPk = 3,
          assessmentType = "LAYER1",
          initiationDate = initiationDate,
          status = "COMPLETE",
          completedDate = dateCompleted,
        ),
        TimelineDto(
          assessmentPk = 4,
          assessmentType = "LAYER1",
          initiationDate = initiationDate.plusDays(1),
          status = "COMPLETE",
          completedDate = dateCompleted.plusDays(1),
        ),
      ),
    )
    every { oasysApiRestClient.getRiskManagementPlan(any(), any()) }.returns(oasysRiskManagementPlans)

    val result = riskManagementPlanService.getRiskManagementPlans(crn)

    verify(exactly = 1) { oasysApiRestClient.getRiskManagementPlan(crn, "ALLOW") }
    assertThat(result)
      .isEqualTo(
        RiskManagementPlansDto(
          crn = crn,
          limitedAccessOffender = false,
          riskManagementPlan = listOf(
            RiskManagementPlanDto(
              assessmentId = 1,
              assessmentType = "LAYER1",
              dateCompleted = LocalDateTime.of(2022, 1, 5, 12, 0),
              initiationDate = LocalDateTime.of(2022, 1, 1, 12, 0),
              assessmentStatus = "LOCKED_INCOMPLETE",
              partcompStatus = "Signed",
            ),
            RiskManagementPlanDto(
              assessmentId = 2,
              assessmentType = "LAYER1",
              dateCompleted = null,
              initiationDate = LocalDateTime.of(2022, 1, 2, 12, 0),
              assessmentStatus = "SIGNED",
            ),
            RiskManagementPlanDto(
              assessmentId = 3,
              assessmentType = "LAYER1",
              dateCompleted = dateCompleted,
              initiationDate = initiationDate,
              assessmentStatus = "COMPLETE",
              keyInformationCurrentSituation = "patternOfOffending",
              furtherConsiderationsCurrentSituation = "",
              supervision = "",
              monitoringAndControl = "",
              interventionsAndTreatment = "",
              victimSafetyPlanning = "",
              contingencyPlans = "",
              laterWIPAssessmentExists = false,
              latestWIPDate = null,
            ),
            RiskManagementPlanDto(
              assessmentId = 4,
              assessmentType = "LAYER1",
              dateCompleted = dateCompleted.plusDays(1),
              initiationDate = initiationDate.plusDays(1),
              assessmentStatus = "COMPLETE",
            ),
          ),
        ),
      )
  }

  @Test
  fun `should throw exception when crn is not found for given CRN`() {
    // Given
    val crn = "DOES_NOT_EXIST"
    every {
      oasysApiRestClient.getRiskManagementPlan(
        any(),
        any(),
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
      oasysApiRestClient.getRiskManagementPlan(
        any(),
        any(),
      )
    }.throws(EntityNotFoundException("No matching completed risk management plan found for this offender"))

    // When
    assertThrows<EntityNotFoundException> {
      riskManagementPlanService.getRiskManagementPlans(crn)
    }
  }

  @Test
  fun `should NOT call get risk management plan when user is forbidden to access CRN`() {
    // Given
    val crn = "X12345"
    every { communityClient.verifyUserAccess(crn, any()) }.throws(
      ExternalApiForbiddenException(
        "User does not have permission to access offender with CRN $crn.",
        HttpMethod.GET,
        SimpleCacheFactory.path,
        ExternalService.COMMUNITY_API,
        listOfNotNull("Excluded", "Restricted"),
      ),
    )

    // When
    assertThrows<ExternalApiForbiddenException> { riskManagementPlanService.getRiskManagementPlans(crn) }

    // Then
    verify(exactly = 0) { oasysApiRestClient.getRiskManagementPlan(crn, "ALLOW") }
  }
}
