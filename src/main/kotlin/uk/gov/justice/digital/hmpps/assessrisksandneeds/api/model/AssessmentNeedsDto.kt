package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import io.swagger.v3.oas.annotations.media.Schema
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
      offenderNeedsDto: OffenderNeedsDto,
    ): AssessmentNeedsDto {
      val unansweredNeeds = mutableListOf<AssessmentNeedDto>()
      val identifiedNeeds = mutableListOf<AssessmentNeedDto>()
      val notIdentifiedNeeds = mutableListOf<AssessmentNeedDto>()

      for (needDto in needs) {
        when (needDto.identifiedAsNeed) {
          null -> unansweredNeeds.add(needDto)
          true -> identifiedNeeds.add(needDto)
          false -> notIdentifiedNeeds.add(needDto)
        }
      }
      return AssessmentNeedsDto(
        identifiedNeeds = identifiedNeeds,
        notIdentifiedNeeds = notIdentifiedNeeds,
        unansweredNeeds = unansweredNeeds,
        assessedOn = offenderNeedsDto.assessedOn,
      )
    }
  }
}

data class AssessmentNeedDto(
  @Schema(description = "The section of the need in oasys", example = "DRUG_MISUSE")
  val section: String? = null,
  @Schema(description = "The name of the section need", example = "Drug misuse")
  val name: String? = null,
  @Schema(description = "Represents whether the weighted score of the section is over the threshold", example = "true")
  val overThreshold: Boolean? = null,
  @Schema(description = "Whether the section answers indicate a risk of harm", example = "false")
  val riskOfHarm: Boolean? = null,
  @Schema(description = "Whether the section answers indicate a risk of reoffending", example = "false")
  val riskOfReoffending: Boolean? = null,
  @Schema(description = "Whether the section has been flagged as a low scoring need", example = "true")
  val flaggedAsNeed: Boolean? = null,
  @Schema(description = "The calculated severity of the need", example = "true")
  val severity: NeedSeverity? = null,
  @Schema(description = "Whether the section questions indicate that this section is a need", example = "true")
  val identifiedAsNeed: Boolean? = null,
  @Schema(description = "The weighted score for the section", example = "4")
  val needScore: Long? = null,
) {
  companion object {

    fun from(section: String, offenderNeedDto: OffenderNeedDto): AssessmentNeedDto {
      with(offenderNeedDto) {
        return AssessmentNeedDto(
          section = section,
          name = name,
          overThreshold = overThreshold,
          riskOfHarm = riskOfHarm,
          riskOfReoffending = riskOfReoffending,
          flaggedAsNeed = flaggedAsNeed,
          severity = severity,
          identifiedAsNeed = identifiedAsNeed,
          needScore = needScore,
        )
      }
    }

    fun from(section: String, sectionMap: Map<String, String>): AssessmentNeedDto {
      return AssessmentNeedDto(
        section = section,
        name = sectionMap[section],
      )
    }
  }
}

enum class NeedSeverity {
  NO_NEED,
  STANDARD,
  SEVERE,
}
