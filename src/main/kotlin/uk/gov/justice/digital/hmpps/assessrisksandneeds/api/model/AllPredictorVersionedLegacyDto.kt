package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysRiskPredictorsDto
import java.time.LocalDateTime

data class AllPredictorVersionedLegacyDto(
  override val completedDate: LocalDateTime? = null,
  override val status: AssessmentStatus,
  override val version: Int? = null,
  override val output: RiskScoresDto? = null,
) : AllPredictorVersioned<RiskScoresDto> {
  companion object {
    fun from(oasysRiskPredictorsDto: OasysRiskPredictorsDto?): List<AllPredictorVersionedLegacyDto> = oasysRiskPredictorsDto?.assessments?.filter { it.assessmentType in listOf("LAYER3", "LAYER1") }?.map { assessment ->
      AllPredictorVersionedLegacyDto(
        completedDate = assessment.dateCompleted,
        status = assessment.assessmentStatus,
        version = 1,
        output = RiskScoresDto.fromVersioned(assessment),
      )
    }.orEmpty()
  }
}
