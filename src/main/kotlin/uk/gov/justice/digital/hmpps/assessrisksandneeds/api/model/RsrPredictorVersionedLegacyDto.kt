package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.AllRisksPredictorAssessmentDto
import java.time.LocalDateTime

data class RsrPredictorVersionedLegacyDto(
  override val completedDate: LocalDateTime? = null,
  override val source: RsrScoreSource,
  override val status: AssessmentStatus,
  override val outputVersion: String = "1",
  override val output: RsrPredictorDto? = null,
) : RsrPredictorVersioned<RsrPredictorDto> {

  companion object {

    fun from(oasysPredictorsDtos: List<AllRisksPredictorAssessmentDto>): List<RsrPredictorVersionedLegacyDto> = oasysPredictorsDtos.map { from(it) }

    fun from(oasysPredictorsDto: AllRisksPredictorAssessmentDto): RsrPredictorVersionedLegacyDto = RsrPredictorVersionedLegacyDto(
      completedDate = oasysPredictorsDto.dateCompleted,
      source = RsrScoreSource.OASYS,
      status = oasysPredictorsDto.assessmentStatus,
      output = RsrPredictorDto.fromVersioned(oasysPredictorsDto),
    )
  }
}
