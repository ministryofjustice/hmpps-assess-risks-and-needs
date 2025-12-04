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
  @JsonProperty("OGRS4G")
  val ogrs4gScoreDto: OasysOgrs4gDto?,
  @JsonProperty("OGRS4V")
  val ogrs4vScoreDto: OasysOgrs4vDto?,
  @JsonProperty("OGP2")
  val ogp2ScoreDto: OasysOgp2Dto?,
  @JsonProperty("OVP2")
  val ovp2ScoreDto: OasysOvp2Dto?,
  @JsonProperty("SNSV")
  val snsvScoreDto: OasysSnsvDto?,
)
