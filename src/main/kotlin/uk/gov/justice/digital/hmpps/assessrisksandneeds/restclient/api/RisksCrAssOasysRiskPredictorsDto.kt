package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api

import com.fasterxml.jackson.annotation.JsonProperty

data class RisksCrAssOasysRiskPredictorsDto(
  var assessments: List<RisksCrAssPredictorAssessmentDto>? = emptyList(),
)

data class RisksCrAssPredictorAssessmentDto(
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
  @JsonProperty("NEWACTPREDICT")
  val newAllPredictorScoresDto: OasysNewAllPredictorDto? = null,
)
