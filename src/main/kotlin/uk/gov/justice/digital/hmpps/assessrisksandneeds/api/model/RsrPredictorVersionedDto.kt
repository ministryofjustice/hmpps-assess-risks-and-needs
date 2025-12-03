package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.AllRisksPredictorAssessmentDto
import java.time.LocalDateTime

data class RsrPredictorVersionedDto(
  override val completedDate: LocalDateTime? = null,
  override val source: RsrScoreSource,
  override val status: AssessmentStatus,
  @Schema(description = "Version of the output", allowableValues = ["2"], defaultValue = "2")
  override val outputVersion: String = "2",
  override val output: Any? = null, // TODO: Update Any to new parent Predictor models list
) : RsrPredictorVersioned<Any> { // TODO: Update Any to new parent Predictor models list

  companion object {

    fun from(oasysPredictorsDtos: List<AllRisksPredictorAssessmentDto>): List<RsrPredictorVersionedDto> = oasysPredictorsDtos.map { from(it) }

    fun from(oasysPredictorsDto: AllRisksPredictorAssessmentDto): RsrPredictorVersionedDto = RsrPredictorVersionedDto(
      completedDate = oasysPredictorsDto.dateCompleted,
      source = RsrScoreSource.OASYS,
      status = oasysPredictorsDto.assessmentStatus,
      output = null, // TODO: Build new RSR associated parent Predictor models list
    )
  }
}
