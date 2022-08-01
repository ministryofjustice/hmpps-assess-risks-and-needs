package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.CommonAssessmentDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OffenceDetailDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.VictimDetailDto
import java.time.LocalDateTime

data class OasysAssessmentOffenceDto(

  val crn: String,
  val limitedAccessOffender: Boolean,
  var assessments: List<OasysAssessmentDto> = emptyList(),
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  var timeline: List<TimelineDto> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
class TimelineDto(

  @JsonProperty("assessmentPk")
  val assessmentPk: Long,
  val assessmentType: String,
  val initiationDate: LocalDateTime,
  val status: String,
  val completedDate: LocalDateTime? = null,
  val partcompStatus: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class OasysAssessmentDto(

  val assessmentPk: Long,
  val assessmentType: String,
  val partcompStatus: String? = null,
  val dateCompleted: LocalDateTime?,
  val initiationDate: LocalDateTime,
  val assessorSignedDate: LocalDateTime? = null,
  val assessmentStatus: String,
  val superStatus: String? = null,
  val offence: String? = null,
  var disinhibitors: List<String> = emptyList(),
  val patternOfOffending: String? = null,
  var offenceInvolved: List<String> = emptyList(),
  val specificWeapon: String? = null,
  val victimPerpetratorRelationship: String? = null,
  val victimOtherInfo: String? = null,
  val evidencedMotivations: List<String> = emptyList(),
  val offenceDetails: List<OffenceDetailDto> = emptyList(),
  val victimDetails: List<VictimDetailDto> = emptyList(),
  laterWIPAssessmentExists: Boolean? = null,
  latestWIPDate: LocalDateTime? = null,
  laterSignLockAssessmentExists: Boolean? = null,
  latestSignLockDate: LocalDateTime? = null,
  laterPartCompUnsignedAssessmentExists: Boolean? = null,
  latestPartCompUnsignedDate: LocalDateTime? = null,
  laterPartCompSignedAssessmentExists: Boolean? = null,
  latestPartCompSignedDate: LocalDateTime? = null,
  laterCompleteAssessmentExists: Boolean? = null,
  latestCompleteDate: LocalDateTime? = null,
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
