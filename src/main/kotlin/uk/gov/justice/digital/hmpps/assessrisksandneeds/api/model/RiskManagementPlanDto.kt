package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysRiskManagementPlanDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.TimelineDto
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class RiskManagementPlanDto(
  val assessmentId: Long,
  val dateCompleted: LocalDateTime?,
  val partcompStatus: String? = null,
  val initiationDate: LocalDateTime,
  val assessmentStatus: String,
  val assessmentType: String,
  val superStatus: String? = null,
  val keyInformationCurrentSituation: String? = null,
  val furtherConsiderationsCurrentSituation: String? = null,
  val supervision: String? = null,
  val monitoringAndControl: String? = null,
  val interventionsAndTreatment: String? = null,
  val victimSafetyPlanning: String? = null,
  val contingencyPlans: String? = null,
  override val laterWIPAssessmentExists: Boolean? = null,
  override val latestWIPDate: LocalDateTime? = null,
  override val laterSignLockAssessmentExists: Boolean? = null,
  override val latestSignLockDate: LocalDateTime? = null,
  override val laterPartCompUnsignedAssessmentExists: Boolean? = null,
  override val latestPartCompUnsignedDate: LocalDateTime? = null,
  override val laterPartCompSignedAssessmentExists: Boolean? = null,
  override val latestPartCompSignedDate: LocalDateTime? = null,
  override val laterCompleteAssessmentExists: Boolean? = null,
  override val latestCompleteDate: LocalDateTime? = null,
) : RelatedAssessmentState {
  companion object {

    fun from(timelineDto: TimelineDto): RiskManagementPlanDto {
      with(timelineDto) {
        return RiskManagementPlanDto(
          assessmentId = assessmentPk,
          assessmentType = assessmentType,
          dateCompleted = completedDate,
          assessmentStatus = status,
          initiationDate = initiationDate,
          partcompStatus = partcompStatus
        )
      }
    }

    fun from(oasysRiskManagementPlanDto: OasysRiskManagementPlanDto): RiskManagementPlanDto {
      with(oasysRiskManagementPlanDto) {
        return RiskManagementPlanDto(
          assessmentId = assessmentPk,
          assessmentType = assessmentType,
          dateCompleted = dateCompleted,
          initiationDate = initiationDate,
          assessmentStatus = assessmentStatus,
          keyInformationCurrentSituation = keyInformationCurrentSituation,
          furtherConsiderationsCurrentSituation = furtherConsiderationsCurrentSituation,
          supervision = supervision,
          monitoringAndControl = monitoringAndControl,
          interventionsAndTreatment = interventionsAndTreatment,
          victimSafetyPlanning = victimSafetyPlanning,
          contingencyPlans = contingencyPlans,
          superStatus = superStatus,
          laterWIPAssessmentExists = laterWIPAssessmentExists,
          latestWIPDate = latestWIPDate,
          laterSignLockAssessmentExists = laterSignLockAssessmentExists,
          latestSignLockDate = latestSignLockDate,
          laterPartCompUnsignedAssessmentExists = laterPartCompUnsignedAssessmentExists,
          latestPartCompUnsignedDate = latestPartCompUnsignedDate,
          laterPartCompSignedAssessmentExists = laterPartCompSignedAssessmentExists,
          latestPartCompSignedDate = latestPartCompSignedDate,
          laterCompleteAssessmentExists = laterCompleteAssessmentExists,
          latestCompleteDate = latestCompleteDate
        )
      }
    }
  }
}
