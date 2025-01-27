package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysRiskManagementPlanDetailsDto

data class RiskManagementPlansDto(
  val crn: String,
  val limitedAccessOffender: Boolean,
  val riskManagementPlan: List<RiskManagementPlanDto> = emptyList(),
) {

  companion object {
    fun from(oasysRiskManagementPlanDetails: OasysRiskManagementPlanDetailsDto): RiskManagementPlansDto {
      val riskManagementPlan = createRiskManagementPlans(oasysRiskManagementPlanDetails)
      val assessmentSummaries = createAssessmentSummaries(oasysRiskManagementPlanDetails, riskManagementPlan.map { it.assessmentId })
      return RiskManagementPlansDto(
        crn = oasysRiskManagementPlanDetails.crn,
        limitedAccessOffender = oasysRiskManagementPlanDetails.limitedAccessOffender,
        riskManagementPlan = (assessmentSummaries + riskManagementPlan).sortedBy { it.initiationDate },
      )
    }

    private fun createRiskManagementPlans(oasysRiskManagementPlanDetails: OasysRiskManagementPlanDetailsDto): List<RiskManagementPlanDto> = oasysRiskManagementPlanDetails.riskManagementPlans.map {
      RiskManagementPlanDto.from(it)
    }

    private fun createAssessmentSummaries(oasysRiskManagementPlanDetails: OasysRiskManagementPlanDetailsDto, riskManagementPlanAssessmentIds: List<Long>): List<RiskManagementPlanDto> = oasysRiskManagementPlanDetails.timeline
      .filterNot { it.assessmentPk in riskManagementPlanAssessmentIds }
      .map {
        RiskManagementPlanDto.from(it)
      }
  }
}
