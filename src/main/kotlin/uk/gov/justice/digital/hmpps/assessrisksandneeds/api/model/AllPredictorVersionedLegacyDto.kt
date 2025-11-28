package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.RiskPredictorAssessmentDto
import java.time.LocalDateTime

data class AllPredictorVersionedLegacyDto(
  override val completedDate: LocalDateTime? = null,
  override val status: AssessmentStatus,
  @Schema(description = "Version of the output", allowableValues = ["1"], defaultValue = "1")
  override val outputVersion: String = "1",
  override val output: RiskScoresDto? = null,
) : AllPredictorVersioned<RiskScoresDto> {
  companion object {
    fun from(assessment: RiskPredictorAssessmentDto): AllPredictorVersionedLegacyDto = AllPredictorVersionedLegacyDto(
      completedDate = assessment.dateCompleted,
      status = assessment.assessmentStatus,
      output = RiskScoresDto.fromVersioned(assessment),
    )
  }
}
