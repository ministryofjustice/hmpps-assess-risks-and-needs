package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ogrs4.AllPredictorDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.RiskPredictorAssessmentDto
import java.time.LocalDateTime

data class AllPredictorVersionedDto(
  override val completedDate: LocalDateTime? = null,
  override val status: AssessmentStatus,
  override val version: Int? = null,
  override val output: AllPredictorDto? = null,
) : AllPredictorVersioned<AllPredictorDto> {
  companion object {
    fun from(assessment: RiskPredictorAssessmentDto): AllPredictorVersionedDto = AllPredictorVersionedDto(
      completedDate = assessment.dateCompleted,
      status = assessment.assessmentStatus,
      version = 2,
      output = AllPredictorDto.from(assessment),
    )
  }
}
