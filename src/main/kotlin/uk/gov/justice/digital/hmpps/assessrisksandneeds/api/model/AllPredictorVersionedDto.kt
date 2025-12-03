package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ogrs4.AllPredictorDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.AllRisksPredictorAssessmentDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.RisksCrAssPredictorAssessmentDto
import java.time.LocalDateTime

data class AllPredictorVersionedDto(
  override val completedDate: LocalDateTime? = null,
  override val status: AssessmentStatus? = null,
  @Schema(description = "Version of the output", allowableValues = ["2"], defaultValue = "2")
  override val outputVersion: String = "2",
  override val output: AllPredictorDto? = null,
) : AllPredictorVersioned<AllPredictorDto> {
  companion object {
    fun from(assessment: AllRisksPredictorAssessmentDto): AllPredictorVersionedDto = AllPredictorVersionedDto(
      completedDate = assessment.dateCompleted,
      status = assessment.assessmentStatus,
      output = AllPredictorDto.from(assessment),
    )
    fun from(assessment: RisksCrAssPredictorAssessmentDto): AllPredictorVersionedDto = AllPredictorVersionedDto(
      output = AllPredictorDto.from(assessment),
    )
  }
}
