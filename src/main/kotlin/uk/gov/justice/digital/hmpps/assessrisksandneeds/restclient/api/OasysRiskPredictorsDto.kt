package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api

import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentStatus
import java.time.LocalDateTime

data class OasysRiskPredictorsDto(
  var assessments: List<RiskPredictorAssessmentDto>? = emptyList(),
)

data class RiskPredictorAssessmentDto(
  val dateCompleted: LocalDateTime,
  val assessmentType: String,
  val assessmentStatus: AssessmentStatus,
  @JsonProperty("OGP")
  val ogpScoreDto: OasysOgpDto,
  @JsonProperty("OVP")
  val ovpScoreDto: OasysOvpDto,
  @JsonProperty("OGRS")
  val ogrScoreDto: OasysOgrDto,
  @JsonProperty("RSR")
  val rsrScoreDto: OasysRsrDto,
  @JsonProperty("OSP")
  val ospScoreDto: OasysOspDto,
) {
  fun hasRsrScores(): Boolean = rsrScoreDto.rsrPercentageScore != null &&
    rsrScoreDto.rsrStaticOrDynamic != null &&
    rsrScoreDto.rsrAlgorithmVersion != null &&
    rsrScoreDto.scoreLevel != null
}
