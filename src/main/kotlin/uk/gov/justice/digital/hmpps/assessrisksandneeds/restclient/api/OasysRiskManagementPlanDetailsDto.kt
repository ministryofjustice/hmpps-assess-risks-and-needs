package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
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
  val dateCompleted: LocalDateTime?,
  val initiationDate: LocalDateTime,
  val assessmentStatus: String,
  val keyConsiderationsCurrentSituation: String?,
  val furtherConsiderationsCurrentSituation: String?,
  val supervision: String?,
  val monitoringAndControl: String?,
  val interventionsAndTreatment: String?,
  val victimSafetyPlanning: String?,
  val contingencyPlans: String?
)
