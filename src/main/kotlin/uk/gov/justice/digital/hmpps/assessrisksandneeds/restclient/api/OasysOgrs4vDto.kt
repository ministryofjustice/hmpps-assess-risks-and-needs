package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysOgrs4vDto(
  val ogrs4vYr2: BigDecimal? = null,
  val ogrs4vBand: String? = null,
  val ogrs4vCalculated: String? = null,
)
