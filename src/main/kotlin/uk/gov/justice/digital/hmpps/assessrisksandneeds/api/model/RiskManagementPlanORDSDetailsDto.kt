package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
class RiskManagementPlanORDSDetailsDto(
  val source: String,
  val crn: String,
  val limitedAccessOffender: String,
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  var timeline: List<TimelineDto> = emptyList(),
  @JsonAlias("assessments")
  val riskManagementPlan: RiskManagementPlanDto
)

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
  class RiskManagementPlanDto(
    val assessmentPk: Long,
    val dateCompleted: LocalDateTime?,
    val initiationDate: LocalDateTime,
    val assessmentStatus: String,
    val keyConsiderationsCurrentSituation: String?,
    val furtherConsiderationsCurrentSituation: String?,
    val supervision: String?,
    val monitoringAndControl: String?,
    val interventionsAndTreatment: String?,
    val victimSafetyPlanning : String?,
    val contingencyPlans: String?
  )