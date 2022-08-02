package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class AssessmentOffenceDto(

  val crn: String,
  val limitedAccessOffender: Boolean,
  var assessments: List<AssessmentDto> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class AssessmentDto(

  val assessmentId: Long,
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
) : RelatedAssessmentState

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
