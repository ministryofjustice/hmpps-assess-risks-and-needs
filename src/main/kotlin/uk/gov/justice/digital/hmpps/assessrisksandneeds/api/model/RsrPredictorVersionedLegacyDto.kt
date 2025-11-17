package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.RiskPredictorAssessmentDto
import java.time.LocalDateTime

data class RsrPredictorVersionedLegacyDto(
  override val completedDate: LocalDateTime? = null,
  override val source: RsrScoreSource,
  override val status: AssessmentStatus,
  override val version: Int? = null,
  override val output: RsrPredictorDto? = null,
) : RsrPredictorVersioned<RsrPredictorDto> {

  companion object {

    fun from(oasysPredictorsDtos: List<RiskPredictorAssessmentDto>): List<RsrPredictorVersionedLegacyDto> = oasysPredictorsDtos.map { from(it) }

    fun from(oasysPredictorsDto: RiskPredictorAssessmentDto): RsrPredictorVersionedLegacyDto = RsrPredictorVersionedLegacyDto(
      completedDate = oasysPredictorsDto.dateCompleted,
      source = RsrScoreSource.OASYS,
      status = oasysPredictorsDto.assessmentStatus,
      version = 1,
      output = RsrPredictorDto.fromVersioned(oasysPredictorsDto),
    )
  }
}
