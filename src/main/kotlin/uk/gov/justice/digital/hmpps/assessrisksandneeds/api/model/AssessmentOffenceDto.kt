package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class AssessmentOffenceDto(

  val crn: String,
  var assessments: List<AssessmentDto> = emptyList(),
  var timeLine: List<TimeLineDto> = emptyList()
)

data class TimeLineDto(

  val initiationDate: LocalDateTime,
  val status: String,
  val completedDate: LocalDateTime,
)

data class AssessmentDto(

  val dateCompleted: LocalDateTime?,
  val initiationDate: LocalDateTime,
  val assessmentStatus: String,
  val offence: String,
  var disinhibitors: List<String> = emptyList(),
  val patternOfOffending: String?,
  var offenceInvolved: List<String> = emptyList(),
  val specificWeapon: String?,
  val victimPerpetratorRelationship: String?,
  val victimOtherInfo: String?,
  val evidencedMotivations: List<String> = emptyList(),
  val offenceDetails: List<OffenceDetailDto> = emptyList(),
  val victimDetails: List<VictimDetailDto> = emptyList(),
)

data class VictimDetailDto(

  val age: String,
  val gender: String,
  val ethnicCategory: String,
  val victimRelation: String
)

data class OffenceDetailDto(

  val type: String,
  val offenceDate: LocalDateTime,
  val offenceCode: String,
  val offenceSubCode: String,
  val offence: String,
  val subOffence: String,
)
