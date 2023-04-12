package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreType
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysRsrDto(
  val rsrPercentageScore: BigDecimal? = null,
  val rsrStaticOrDynamic: ScoreType? = null,
  val rsrAlgorithmVersion: String? = null,
  val scoreLevel: String? = null,
)
