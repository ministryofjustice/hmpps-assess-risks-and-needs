package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api

import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentStatus
import java.time.LocalDateTime

data class TierPredictorsDto(
  var tierPredictors: TierPredictorScoresDto,
  val assessments: List<TierPredictorAssessmentDto>,
)

data class TierPredictorScoresDto(
  @JsonProperty("rsr")
  val rsrScoreDto: OasysRsrDto,
  @JsonProperty("osp")
  val ospScoreDto: OasysOspDto,
  @JsonProperty("newActuarialPredictors")
  val newAllPredictorScoresDto: OasysNewAllPredictorDto,
)

data class TierPredictorAssessmentDto(
  val assessmentPk: Long?,
  val assessmentStatus: AssessmentStatus,
  val assessmentType: String,
  val initiationDate: LocalDateTime,
  val dateCompleted: LocalDateTime? = null,
)
