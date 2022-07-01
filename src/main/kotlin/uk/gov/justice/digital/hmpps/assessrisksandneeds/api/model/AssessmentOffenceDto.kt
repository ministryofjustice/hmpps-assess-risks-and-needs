package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)

data class AssessmentOffenceDto(

  val crn: String,
  var assessments: List<AssessmentDto> = emptyList(),
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  var timeline: List<TimelineDto> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TimelineDto(

  @JsonProperty("assessmentPk")
  val assessmentId: Long,
  val initiationDate: LocalDateTime,
  val status: String,
  val completedDate: LocalDateTime?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class AssessmentDto(

  @JsonProperty("assessmentPk")
  val assessmentId: Long,
  val dateCompleted: LocalDateTime?,
  val initiationDate: LocalDateTime,
  val assessmentStatus: String,
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
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class VictimDetailDto(

  val age: String,
  val gender: String,
  val ethnicCategory: String,
  val victimRelation: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OffenceDetailDto(

  val type: String,
  val offenceDate: LocalDateTime,
  val offenceCode: String,
  val offenceSubCode: String,
  val offence: String,
  val subOffence: String,
)
