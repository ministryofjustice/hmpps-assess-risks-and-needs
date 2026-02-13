package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreType
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysRsrDto(
  val rsrPercentageScore: BigDecimal? = null,
  val rsrStaticOrDynamic: ScoreType? = null,
  val rsrAlgorithmVersion: String? = null,
  @field:JsonProperty("scoreLevel")
  @field:JsonAlias("rsrScoreLevel")
  val scoreLevel: String? = null,
)
