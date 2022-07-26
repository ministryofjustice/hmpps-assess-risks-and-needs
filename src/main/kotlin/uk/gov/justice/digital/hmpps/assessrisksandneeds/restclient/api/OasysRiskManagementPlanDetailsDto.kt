package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.CommonAssessmentDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.TimelineDto
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
class OasysRiskManagementPlanDetailsDto(
  val crn: String,
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
  laterWIPAssessmentExists: Boolean? = null,
  latestWIPDate: LocalDateTime? = null,
  laterSignLockAssessmentExists: Boolean? = null,
  latestSignLockDate: LocalDateTime? = null,
  laterPartCompUnsignedAssessmentExists: Boolean? = null,
  latestPartCompUnsignedDate: LocalDateTime? = null,
  laterPartCompSignedAssessmentExists: Boolean? = null,
  latestPartCompSignedDate: LocalDateTime? = null,
  laterCompleteAssessmentExists: Boolean? = null,
  latestCompleteDate: LocalDateTime? = null
) : CommonAssessmentDto(
  laterWIPAssessmentExists,
  latestWIPDate,
  laterSignLockAssessmentExists,
  latestSignLockDate,
  laterPartCompUnsignedAssessmentExists,
  latestPartCompUnsignedDate,
  laterPartCompSignedAssessmentExists,
  latestPartCompSignedDate,
  laterCompleteAssessmentExists,
  latestCompleteDate
)
