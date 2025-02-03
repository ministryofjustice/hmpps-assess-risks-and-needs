package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.oasys.section.OasysThreshold
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.oasys.section.Threshold
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.oasys.section.TierThreshold
import java.time.LocalDateTime

data class AssessmentNeedsDto(
  @Schema(description = "Collection of assessment need sections which have been answered and identified as needs")
  val identifiedNeeds: Collection<AssessmentNeedDto>,
  @Schema(description = "Collection of assessment need sections which have been answered but are not identified as needs")
  val notIdentifiedNeeds: Collection<AssessmentNeedDto>,
  @Schema(description = "Collection of assessment need sections which have not been answered")
  val unansweredNeeds: Collection<AssessmentNeedDto>,
  @Schema(description = "The date and time that the assessment needs were completed")
  val assessedOn: LocalDateTime,
) {
  companion object {
    fun from(
      needs: Collection<AssessmentNeedDto>,
      assessedOn: LocalDateTime,
    ): AssessmentNeedsDto {
      val unansweredNeeds = mutableListOf<AssessmentNeedDto>()
      val identifiedNeeds = mutableListOf<AssessmentNeedDto>()
      val notIdentifiedNeeds = mutableListOf<AssessmentNeedDto>()

      for (needDto in needs) {
        when (needDto.severity) {
          null -> unansweredNeeds.add(needDto)
          NeedSeverity.STANDARD, NeedSeverity.SEVERE -> identifiedNeeds.add(needDto)
          NeedSeverity.NO_NEED -> notIdentifiedNeeds.add(needDto)
        }
      }
      return AssessmentNeedsDto(
        identifiedNeeds = identifiedNeeds,
        notIdentifiedNeeds = notIdentifiedNeeds,
        unansweredNeeds = unansweredNeeds,
        assessedOn = assessedOn,
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
  @Schema(description = "The calculated severity of the need", example = "SEVERE")
  val severity: NeedSeverity? = null,
  @Schema(description = "The score of the section", example = "3")
  val score: Int? = null,
  @Schema(description = "The thresholds for standard and severe needs from OASys", example = "3")
  val oasysThreshold: OasysThreshold? = null,
  @Schema(description = "The thresholds for standard and severe needs for used for tier", example = "3")
  val tierThreshold: TierThreshold? = null
)

enum class NeedSeverity {
  NO_NEED,
  STANDARD,
  SEVERE,
}
