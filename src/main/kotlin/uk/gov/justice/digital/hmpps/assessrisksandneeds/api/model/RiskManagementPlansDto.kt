package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysRiskManagementPlanDetailsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysRiskManagementPlanDto
import java.time.LocalDateTime

data class RiskManagementPlansDto(
  val crn: String,
  val riskManagementPlan: List<RiskManagementPlanDto> = emptyList()
) {

  companion object {
    fun from(oasysRiskManagementPlanDetails: OasysRiskManagementPlanDetailsDto): RiskManagementPlansDto {
      val riskManagementPlan = createRiskManagementPlans(oasysRiskManagementPlanDetails)
      val assessmentSummaries = createAssessmentSummaries(oasysRiskManagementPlanDetails, riskManagementPlan.map { it.assessmentId })
      return RiskManagementPlansDto(
        crn = oasysRiskManagementPlanDetails.crn,
        riskManagementPlan = (assessmentSummaries + riskManagementPlan).sortedBy { it.initiationDate }
      )
    }

    private fun createRiskManagementPlans(oasysRiskManagementPlanDetails: OasysRiskManagementPlanDetailsDto): List<RiskManagementPlanDto> {
      return oasysRiskManagementPlanDetails.riskManagementPlans.map {
        RiskManagementPlanDto.from(it)
      }
    }

    private fun createAssessmentSummaries(oasysRiskManagementPlanDetails: OasysRiskManagementPlanDetailsDto, riskManagementPlanAssessmentIds: List<Long>): List<RiskManagementPlanDto> {
      return oasysRiskManagementPlanDetails.timeline
        .filterNot { it.assessmentId in riskManagementPlanAssessmentIds }
        .map {
          RiskManagementPlanDto.from(it)
        }
    }
  }
}

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class RiskManagementPlanDto(
  val assessmentId: Long,
  val dateCompleted: LocalDateTime?,
  val initiationDate: LocalDateTime,
  val assessmentStatus: String,
  val keyConsiderationsCurrentSituation: String? = null,
  val furtherConsiderationsCurrentSituation: String? = null,
  val supervision: String? = null,
  val monitoringAndControl: String? = null,
  val interventionsAndTreatment: String? = null,
  val victimSafetyPlanning: String? = null,
  val contingencyPlans: String? = null
) {
  companion object {

    fun from(timelineDto: TimelineDto): RiskManagementPlanDto {
      with(timelineDto) {
        return RiskManagementPlanDto(
          assessmentId = assessmentId,
          dateCompleted = completedDate,
          assessmentStatus = status,
          initiationDate = initiationDate
        )
      }
    }

    fun from(oasysRiskManagementPlanDto: OasysRiskManagementPlanDto): RiskManagementPlanDto {
      with(oasysRiskManagementPlanDto) {
        return RiskManagementPlanDto(
          assessmentId = assessmentPk,
          dateCompleted = dateCompleted,
          initiationDate = initiationDate,
          assessmentStatus = assessmentStatus,
          keyConsiderationsCurrentSituation = keyConsiderationsCurrentSituation,
          furtherConsiderationsCurrentSituation = furtherConsiderationsCurrentSituation,
          supervision = supervision,
          monitoringAndControl = monitoringAndControl,
          interventionsAndTreatment = interventionsAndTreatment,
          victimSafetyPlanning = victimSafetyPlanning,
          contingencyPlans = contingencyPlans
        )
      }
    }
  }
}
