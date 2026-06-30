package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.oasys.section.OasysThreshold
import java.time.LocalDateTime

data class AssessmentNeedsDto(
  @Schema(description = "Collection of assessment need sections which have been answered and identified as needs")
  val identifiedNeeds: Collection<AssessmentNeedDto>,
  @Schema(description = "Collection of assessment need sections which have been answered but are not identified as needs")
  val notIdentifiedNeeds: Collection<AssessmentNeedDto>,
  @Schema(description = "Collection of assessment need sections which have not been answered")
  val unansweredNeeds: Collection<AssessmentNeedDto>,
  @Schema(description = "Whether the assessment is a traditional OASYS or a SAN assessment", example = "OASYS")
  val assessmentVersion: AssessmentVersion,
  @Schema(description = "The date and time that the assessment needs were completed")
  val assessedOn: LocalDateTime?,
) {
  companion object {
    fun from(assessment: CriminogenicNeedsAssessmentOasys): AssessmentNeedsDto {
      val version = assessment.assessmentVersion.toAssessmentVersion()
      val sectionNeeds = when (version) {
        AssessmentVersion.OASYS -> assessment.oasysNeeds()
        AssessmentVersion.SAN -> assessment.sanNeeds()
      }

      val (answered, unanswered) = sectionNeeds.partition { it.score != null }
      // A section with no OASys threshold can never clear the bar, so it falls into not-identified.
      val (identified, notIdentified) = answered.partition { it.score!! >= (it.oasysThreshold?.standard ?: Int.MAX_VALUE) }

      return AssessmentNeedsDto(
        identifiedNeeds = identified,
        notIdentifiedNeeds = notIdentified,
        unansweredNeeds = unanswered,
        assessmentVersion = version,
        assessedOn = assessment.dateCompleted,
      )
    }
  }
}

data class AssessmentNeedDto(
  @Schema(description = "The section of the need in oasys", example = "DRUG_MISUSE")
  val section: String? = null,
  @Schema(description = "The name of the section need", example = "Drug misuse")
  val name: String? = null,
  @Schema(description = "Whether the section answers indicate a risk of harm", example = "false")
  val riskOfHarm: Boolean? = null,
  @Schema(description = "Whether the section answers indicate a risk of reoffending", example = "false")
  val riskOfReoffending: Boolean? = null,
  @Schema(description = "The score of the section", example = "3")
  val score: Int? = null,
  @Schema(description = "The OASys threshold; a section is an identified need when its score is at or above this value", example = "3")
  val oasysThreshold: OasysThreshold? = null,
)

enum class NeedSeverity {
  NO_NEED,
  STANDARD,
  SEVERE,
}

private fun String?.toAssessmentVersion(): AssessmentVersion = when (this) {
  "1" -> AssessmentVersion.OASYS
  "2" -> AssessmentVersion.SAN
  else -> throw IllegalStateException("Unrecognised assessment version: $this")
}

private fun String?.toYesNoBoolean(): Boolean? = when (this?.uppercase()) {
  "YES" -> true
  "NO" -> false
  else -> null
}

private fun need(
  section: AssessmentSection,
  name: String,
  score: Int?,
  threshold: Int?,
  linkedToHarm: String?,
  linkedToReoffending: String?,
): AssessmentNeedDto = AssessmentNeedDto(
  section = section.name,
  name = name,
  riskOfHarm = linkedToHarm.toYesNoBoolean(),
  riskOfReoffending = linkedToReoffending.toYesNoBoolean(),
  score = score,
  oasysThreshold = threshold?.let { OasysThreshold(it) },
)

private fun CriminogenicNeedsAssessmentOasys.oasysNeeds(): List<AssessmentNeedDto> = listOf(
  need(AssessmentSection.ACCOMMODATION, "Accommodation", acc?.accOtherWeightedScore, acc?.accThreshold, acc?.accLinkedToHarm, acc?.accLinkedToReoffending),
  need(AssessmentSection.EDUCATION_TRAINING_AND_EMPLOYABILITY, "Education, Training and Employability", eTE?.eTEOtherWeightedScore, eTE?.eTEThreshold, eTE?.eTELinkedToHarm, eTE?.eTELinkedToReoffending),
  need(AssessmentSection.RELATIONSHIPS, "Relationships", rel?.relOtherWeightedScore, rel?.relThreshold, rel?.relLinkedToHarm, rel?.relLinkedToReoffending),
  need(AssessmentSection.LIFESTYLE_AND_ASSOCIATES, "Lifestyle and Associates", lifestyle?.lifestyleOtherWeightedScore, lifestyle?.lifestyleThreshold, lifestyle?.lifestyleLinkedToHarm, lifestyle?.lifestyleLinkedToReoffending),
  need(AssessmentSection.DRUG_MISUSE, "Drug Misuse", drug?.drugOtherWeightedScore, drug?.drugThreshold, drug?.drugLinkedToHarm, drug?.drugLinkedToReoffending),
  need(AssessmentSection.ALCOHOL_MISUSE, "Alcohol Misuse", alcohol?.alcoholOtherWeightedScore, alcohol?.alcoholThreshold, alcohol?.alcoholLinkedToHarm, alcohol?.alcoholLinkedToReoffending),
  need(AssessmentSection.THINKING_AND_BEHAVIOUR, "Thinking and Behaviour", think?.thinkOtherWeightedScore, think?.thinkThreshold, think?.thinkLinkedToHarm, think?.thinkLinkedToReoffending),
  need(AssessmentSection.ATTITUDE, "Attitudes", att?.attOtherWeightedScore, att?.attThreshold, att?.attLinkedToHarm, att?.attLinkedToReoffending),
)

private fun CriminogenicNeedsAssessmentOasys.sanNeeds(): List<AssessmentNeedDto> {
  val san = sanCrimNeedScore

  return listOf(
    need(AssessmentSection.ACCOMMODATION, "Accommodation", san?.accomSan?.accomSanScore, san?.accomSan?.accomSanThreshold, san?.accomSan?.accomSanLinkedToHarm, san?.accomSan?.accomSanLinkedToReoffending),
    need(AssessmentSection.EMPLOYMENT_AND_EDUCATION, "Employment and education", san?.empAndEduSan?.empAndEduSanScore, san?.empAndEduSan?.empAndEduSanThreshold, san?.empAndEduSan?.empAndEduSanLinkedToHarm, san?.empAndEduSan?.empAndEduSanLinkedToReoffending),
    need(AssessmentSection.PERSONAL_RELATIONSHIPS_AND_COMMUNITY, "Personal relationships and community", san?.persRelAndCommSan?.persRelAndCommSanScore, san?.persRelAndCommSan?.persRelAndCommSanThreshold, san?.persRelAndCommSan?.persRelAndCommSanLinkedToHarm, san?.persRelAndCommSan?.persRelAndCommSanLinkedToReoffending),
    // SAN does not link lifestyle & associates to harm or reoffending, so both are always null.
    need(AssessmentSection.LIFESTYLE_AND_ASSOCIATES, "Lifestyle and associates", san?.lifeAndAssocSan?.lifeAndAssocSanScore, san?.lifeAndAssocSan?.lifeAndAssocSanThreshold, null, null),
    need(AssessmentSection.DRUG_USE, "Drug use", san?.drugUseSan?.drugUseSanScore, san?.drugUseSan?.drugUseSanThreshold, san?.drugUseSan?.drugUseSanLinkedToHarm, san?.drugUseSan?.drugUseSanLinkedToReoffending),
    need(AssessmentSection.ALCOHOL_USE, "Alcohol use", san?.alcoUseSan?.alcoUseSanScore, san?.alcoUseSan?.alcoUseSanThreshold, san?.alcoUseSan?.alcoUseSanLinkedToHarm, san?.alcoUseSan?.alcoUseSanLinkedToReoffending),
    need(AssessmentSection.THINKING_ATTITUDES_AND_BEHAVIOUR, "Thinking, behaviours and attitudes", san?.thinkBehavAndAttiSan?.thinkBehavAndAttiSanScore, san?.thinkBehavAndAttiSan?.thinkBehavAndAttiSanThreshold, san?.thinkBehavAndAttiSan?.thinkBehavAndAttiSanLinkedToHarm, san?.thinkBehavAndAttiSan?.thinkBehavAndAttiSanLinkedToReoffending),
  )
}
