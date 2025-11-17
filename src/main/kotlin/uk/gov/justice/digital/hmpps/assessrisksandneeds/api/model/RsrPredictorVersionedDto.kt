package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.RiskPredictorAssessmentDto
import java.time.LocalDateTime

data class RsrPredictorVersionedDto(
  override val calculatedDate: LocalDateTime? = null,
  override val completedDate: LocalDateTime? = null,
  override val signedDate: LocalDateTime? = null,
  override val source: RsrScoreSource,
  override val status: AssessmentStatus,
  override val version: Int? = null,
  override val output: Any? = null, // TODO: Update Any to new RSR Predictor models list
) : RsrPredictorVersioned<Any> { // TODO: Update Any to new RSR Predictor models list

  companion object {

    fun from(oasysPredictorsDtos: List<RiskPredictorAssessmentDto>): List<RsrPredictorVersionedDto> = oasysPredictorsDtos.map { from(it) }

    fun from(oasysPredictorsDto: RiskPredictorAssessmentDto): RsrPredictorVersionedDto = RsrPredictorVersionedDto(
      calculatedDate = null,
      completedDate = oasysPredictorsDto.dateCompleted,
      signedDate = null,
      source = RsrScoreSource.OASYS,
      status = oasysPredictorsDto.assessmentStatus,
      version = 1,
      output = null, // TODO: Build new RSR Predictor models list
    )
  }
}
