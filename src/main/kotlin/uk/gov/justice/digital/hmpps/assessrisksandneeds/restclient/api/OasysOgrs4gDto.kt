package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysOgrs4gDto(
  val ogrs4gYr2: BigDecimal? = null,
  val ogrs4gBand: String? = null,
  val ogrs4gCalculated: String? = null,
)
