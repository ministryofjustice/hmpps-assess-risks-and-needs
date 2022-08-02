package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RelatedAssessmentState
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
class OasysRiskManagementPlanDetailsDto(
  val crn: String,
  val limitedAccessOffender: Boolean,
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  var timeline: List<TimelineDto> = emptyList(),
  @JsonAlias("assessments")
  val riskManagementPlans: List<OasysRiskManagementPlanDto>
)

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class OasysRiskManagementPlanDto(
  val assessmentPk: Long,
  val assessmentType: String,
  val dateCompleted: LocalDateTime?,
  val initiationDate: LocalDateTime,
  val assessmentStatus: String,
  val superStatus: String? = null,
  val keyInformationCurrentSituation: String?,
  val furtherConsiderationsCurrentSituation: String?,
  val supervision: String?,
  val monitoringAndControl: String?,
  val interventionsAndTreatment: String?,
  val victimSafetyPlanning: String?,
  val contingencyPlans: String?,
  override val laterWIPAssessmentExists: Boolean? = null,
  override val latestWIPDate: LocalDateTime? = null,
  override val laterSignLockAssessmentExists: Boolean? = null,
  override val latestSignLockDate: LocalDateTime? = null,
  override val laterPartCompUnsignedAssessmentExists: Boolean? = null,
  override val latestPartCompUnsignedDate: LocalDateTime? = null,
  override val laterPartCompSignedAssessmentExists: Boolean? = null,
  override val latestPartCompSignedDate: LocalDateTime? = null,
  override val laterCompleteAssessmentExists: Boolean? = null,
  override val latestCompleteDate: LocalDateTime? = null
) : RelatedAssessmentState
