package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskManagementPlanORDSDetailsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.AssessmentApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.EntityNotFoundException

@Service
class RiskManagementPlanService(private val assessmentClient: AssessmentApiRestClient) {

  private val limitedAccess = "LIMIT"
  private val completeStatus = "COMPLETE"

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }


  fun getRiskManagementPlans(crn: String): RiskManagementPlanORDSDetailsDto {
    log.info("Get assessment offence for CRN: $crn")

    val riskManagementPlanDto = assessmentClient.getRiskManagementPlan(
      crn = crn,
      limitedAccessOffender = limitedAccess
    ) ?: throw EntityNotFoundException("Risk Management Plan not found for CRN: $crn")

    return riskManagementPlanDto
  }
}
