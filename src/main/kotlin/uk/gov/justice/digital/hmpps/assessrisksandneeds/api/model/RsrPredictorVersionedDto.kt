package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ogrs4.RsrPredictorDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.AllRisksPredictorAssessmentDto
import java.time.LocalDateTime

data class RsrPredictorVersionedDto(
  override val completedDate: LocalDateTime? = null,
  override val source: RsrScoreSource,
  override val status: AssessmentStatus,
  override val outputVersion: String = "2",
  override val output: RsrPredictorDto? = null,
) : RsrPredictorVersioned<RsrPredictorDto> {

  companion object {

    fun from(oasysPredictorsDtos: List<AllRisksPredictorAssessmentDto>): List<RsrPredictorVersionedDto> = oasysPredictorsDtos.map { from(it) }

    fun from(oasysPredictorsDto: AllRisksPredictorAssessmentDto): RsrPredictorVersionedDto = RsrPredictorVersionedDto(
      completedDate = oasysPredictorsDto.dateCompleted,
      source = RsrScoreSource.OASYS,
      status = oasysPredictorsDto.assessmentStatus,
      output = RsrPredictorDto.from(oasysPredictorsDto),
    )
  }
}
