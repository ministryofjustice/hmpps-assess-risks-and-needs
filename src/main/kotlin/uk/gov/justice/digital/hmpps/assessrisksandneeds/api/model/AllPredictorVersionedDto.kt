package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysRiskPredictorsDto
import java.time.LocalDateTime

data class AllPredictorVersionedDto(
  override val completedDate: LocalDateTime? = null,
  override val status: AssessmentStatus,
  override val version: Int? = null,
  override val output: Any? = null,
) : AllPredictorVersioned<Any> {
  companion object {
    fun from(oasysRiskPredictorsDto: OasysRiskPredictorsDto?): List<AllPredictorVersionedDto> = oasysRiskPredictorsDto?.assessments?.filter { it.assessmentType in listOf("LAYER3", "LAYER1") }?.map { assessment ->
      AllPredictorVersionedDto(
        completedDate = assessment.dateCompleted,
        status = assessment.assessmentStatus,
        version = 2,
        output = null, // TODO: Build new All Predictor models list
      )
    }.orEmpty()
  }
}
