package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.oasys.section.OasysThreshold
import java.time.LocalDateTime

/**
 * Detailed criminogenic needs view used exclusively by the HMPPS External API (MAPPS).
 *
 * Unlike [AssessmentNeedsDto], this returns every section found in OASys (or its SAN equivalent) in a single
 * `needs` list - including the sections that never carry a criminogenic need score (Finance and Emotional
 * wellbeing for OASys, Finance and Health and wellbeing for SAN) - and labels each with a [NeedStatus].
 */
data class AssessmentNeedsDetailsDto(
  @Schema(description = "Every assessment need section found in OASys, each labelled with its need status")
  val needs: Collection<AssessmentNeedDetailDto>,
  @Schema(description = "Whether the assessment is a traditional OASYS or a SAN assessment", example = "OASYS")
  val assessmentVersion: AssessmentVersion,
  @Schema(description = "The date and time that the assessment needs were completed")
  val assessedOn: LocalDateTime?,
) {
  companion object {
    fun from(assessment: CriminogenicNeedsAssessmentOasys): AssessmentNeedsDetailsDto {
      val version = assessment.assessmentVersion.toAssessmentVersion()
      val needs = when (version) {
        AssessmentVersion.OASYS -> assessment.oasysNeedDetails()
        AssessmentVersion.SAN -> assessment.sanNeedDetails()
      }.sortedBy { it.needStatus.ordinal }

      return AssessmentNeedsDetailsDto(
        needs = needs,
        assessmentVersion = version,
        assessedOn = assessment.dateCompleted,
      )
    }
  }
}

data class AssessmentNeedDetailDto(
  @Schema(description = "The section of the need in oasys", example = "DRUG_MISUSE")
  val section: String? = null,
  @Schema(description = "The name of the section need", example = "Drug misuse")
  val name: String? = null,
  @Schema(description = "The need status of the section", example = "IDENTIFIED_NEED")
  val needStatus: NeedStatus,
  @Schema(description = "Whether the section answers indicate a risk of harm", example = "false")
  val riskOfHarm: Boolean? = null,
  @Schema(description = "Whether the section answers indicate a risk of reoffending", example = "false")
  val riskOfReoffending: Boolean? = null,
  @Schema(description = "The score of the section", example = "3")
  val score: Int? = null,
  @Schema(description = "The OASys threshold; a section is an identified need when its score is at or above this value", example = "3")
  val oasysThreshold: OasysThreshold? = null,
)

enum class NeedStatus {
  IDENTIFIED_NEED,
  NOT_IDENTIFIED_NEED,
  UNANSWERED_NEED,
  UNSCORED_NEED,
}

private fun scoredStatus(score: Int?, threshold: Int?): NeedStatus = when {
  score == null -> NeedStatus.UNANSWERED_NEED
  // A section with no OASys threshold can never clear the bar, so it falls into not-identified.
  score >= (threshold ?: Int.MAX_VALUE) -> NeedStatus.IDENTIFIED_NEED
  else -> NeedStatus.NOT_IDENTIFIED_NEED
}

private fun scoredNeed(
  section: AssessmentSection,
  name: String,
  score: Int?,
  threshold: Int?,
  linkedToHarm: String?,
  linkedToReoffending: String?,
): AssessmentNeedDetailDto = AssessmentNeedDetailDto(
  section = section.name,
  name = name,
  needStatus = scoredStatus(score, threshold),
  riskOfHarm = linkedToHarm.toYesNoBoolean(),
  riskOfReoffending = linkedToReoffending.toYesNoBoolean(),
  score = score,
  oasysThreshold = threshold?.let { OasysThreshold(it) },
)

private fun unscoredNeed(
  section: AssessmentSection,
  name: String,
  linkedToHarm: String?,
  linkedToReoffending: String?,
): AssessmentNeedDetailDto = AssessmentNeedDetailDto(
  section = section.name,
  name = name,
  needStatus = NeedStatus.UNSCORED_NEED,
  riskOfHarm = linkedToHarm.toYesNoBoolean(),
  riskOfReoffending = linkedToReoffending.toYesNoBoolean(),
  score = null,
  oasysThreshold = null,
)

private fun CriminogenicNeedsAssessmentOasys.oasysNeedDetails(): List<AssessmentNeedDetailDto> = listOf(
  scoredNeed(AssessmentSection.ACCOMMODATION, "Accommodation", acc?.accOtherWeightedScore, acc?.accThreshold, acc?.accLinkedToHarm, acc?.accLinkedToReoffending),
  scoredNeed(AssessmentSection.EDUCATION_TRAINING_AND_EMPLOYABILITY, "Education, Training and Employability", eTE?.eTEOtherWeightedScore, eTE?.eTEThreshold, eTE?.eTELinkedToHarm, eTE?.eTELinkedToReoffending),
  scoredNeed(AssessmentSection.RELATIONSHIPS, "Relationships", rel?.relOtherWeightedScore, rel?.relThreshold, rel?.relLinkedToHarm, rel?.relLinkedToReoffending),
  scoredNeed(AssessmentSection.LIFESTYLE_AND_ASSOCIATES, "Lifestyle and Associates", lifestyle?.lifestyleOtherWeightedScore, lifestyle?.lifestyleThreshold, lifestyle?.lifestyleLinkedToHarm, lifestyle?.lifestyleLinkedToReoffending),
  scoredNeed(AssessmentSection.DRUG_MISUSE, "Drug Misuse", drug?.drugOtherWeightedScore, drug?.drugThreshold, drug?.drugLinkedToHarm, drug?.drugLinkedToReoffending),
  scoredNeed(AssessmentSection.ALCOHOL_MISUSE, "Alcohol Misuse", alcohol?.alcoholOtherWeightedScore, alcohol?.alcoholThreshold, alcohol?.alcoholLinkedToHarm, alcohol?.alcoholLinkedToReoffending),
  scoredNeed(AssessmentSection.THINKING_AND_BEHAVIOUR, "Thinking and Behaviour", think?.thinkOtherWeightedScore, think?.thinkThreshold, think?.thinkLinkedToHarm, think?.thinkLinkedToReoffending),
  scoredNeed(AssessmentSection.ATTITUDE, "Attitudes", att?.attOtherWeightedScore, att?.attThreshold, att?.attLinkedToHarm, att?.attLinkedToReoffending),
  unscoredNeed(AssessmentSection.FINANCE, "Finance", finance?.financeLinkedToHarm, finance?.financeLinkedToReoffending),
  unscoredNeed(AssessmentSection.EMOTIONAL_WELLBEING, "Emotional Well-being", emo?.emoLinkedToHarm, emo?.emoLinkedToReoffending),
)

private fun CriminogenicNeedsAssessmentOasys.sanNeedDetails(): List<AssessmentNeedDetailDto> {
  val san = sanCrimNeedScore

  return listOf(
    scoredNeed(AssessmentSection.ACCOMMODATION, "Accommodation", san?.accomSan?.accomSanScore, san?.accomSan?.accomSanThreshold, san?.accomSan?.accomSanLinkedToHarm, san?.accomSan?.accomSanLinkedToReoffending),
    scoredNeed(AssessmentSection.EMPLOYMENT_AND_EDUCATION, "Employment and education", san?.empAndEduSan?.empAndEduSanScore, san?.empAndEduSan?.empAndEduSanThreshold, san?.empAndEduSan?.empAndEduSanLinkedToHarm, san?.empAndEduSan?.empAndEduSanLinkedToReoffending),
    scoredNeed(AssessmentSection.PERSONAL_RELATIONSHIPS_AND_COMMUNITY, "Personal relationships and community", san?.persRelAndCommSan?.persRelAndCommSanScore, san?.persRelAndCommSan?.persRelAndCommSanThreshold, san?.persRelAndCommSan?.persRelAndCommSanLinkedToHarm, san?.persRelAndCommSan?.persRelAndCommSanLinkedToReoffending),
    // SAN does not link lifestyle & associates to harm or reoffending, so both are always null.
    scoredNeed(AssessmentSection.LIFESTYLE_AND_ASSOCIATES, "Lifestyle and associates", san?.lifeAndAssocSan?.lifeAndAssocSanScore, san?.lifeAndAssocSan?.lifeAndAssocSanThreshold, null, null),
    scoredNeed(AssessmentSection.DRUG_USE, "Drug use", san?.drugUseSan?.drugUseSanScore, san?.drugUseSan?.drugUseSanThreshold, san?.drugUseSan?.drugUseSanLinkedToHarm, san?.drugUseSan?.drugUseSanLinkedToReoffending),
    scoredNeed(AssessmentSection.ALCOHOL_USE, "Alcohol use", san?.alcoUseSan?.alcoUseSanScore, san?.alcoUseSan?.alcoUseSanThreshold, san?.alcoUseSan?.alcoUseSanLinkedToHarm, san?.alcoUseSan?.alcoUseSanLinkedToReoffending),
    scoredNeed(AssessmentSection.THINKING_ATTITUDES_AND_BEHAVIOUR, "Thinking, behaviours and attitudes", san?.thinkBehavAndAttiSan?.thinkBehavAndAttiSanScore, san?.thinkBehavAndAttiSan?.thinkBehavAndAttiSanThreshold, san?.thinkBehavAndAttiSan?.thinkBehavAndAttiSanLinkedToHarm, san?.thinkBehavAndAttiSan?.thinkBehavAndAttiSanLinkedToReoffending),
    unscoredNeed(AssessmentSection.FINANCE, "Finance", san?.financeSan?.financeSanLinkedToHarm, san?.financeSan?.financeSanLinkedToReoffending),
    unscoredNeed(AssessmentSection.HEALTH_AND_WELLBEING, "Health and wellbeing", san?.healthAndWellbeingSan?.healthAndWellbeingSanLinkedToHarm, san?.healthAndWellbeingSan?.healthAndWellbeingSanLinkedToReoffending),
  )
}
