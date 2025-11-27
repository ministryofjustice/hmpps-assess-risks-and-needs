package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.RiskPredictorAssessmentDto
import java.time.LocalDateTime

data class AllPredictorVersionedLegacyDto(
  override val completedDate: LocalDateTime? = null,
  override val status: AssessmentStatus,
  override val version: Int? = null,
  override val output: RiskScoresDto? = null,
) : AllPredictorVersioned<RiskScoresDto> {
  companion object {
    fun from(assessment: RiskPredictorAssessmentDto): AllPredictorVersionedLegacyDto = AllPredictorVersionedLegacyDto(
      completedDate = assessment.dateCompleted,
      status = assessment.assessmentStatus,
      version = 1,
      output = RiskScoresDto.fromVersioned(assessment),
    )
  }
}
