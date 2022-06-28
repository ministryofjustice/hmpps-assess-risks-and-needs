package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskManagementPlansDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.AssessmentApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.EntityNotFoundException

@Service
class RiskManagementPlanService(private val assessmentClient: AssessmentApiRestClient) {

  private val limitedAccess = "LIMIT"

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun getRiskManagementPlans(crn: String): RiskManagementPlansDto {
    log.info("Get assessment offence for CRN: $crn")

    val riskManagementPlanOrdsDto = assessmentClient.getRiskManagementPlan(
      crn = crn,
      limitedAccessOffender = limitedAccess
    ) ?: throw EntityNotFoundException("Risk Management Plan not found for CRN: $crn")

    return RiskManagementPlansDto.from(riskManagementPlanOrdsDto)
  }
}