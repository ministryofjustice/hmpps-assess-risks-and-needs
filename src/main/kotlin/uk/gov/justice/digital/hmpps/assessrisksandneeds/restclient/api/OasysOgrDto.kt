package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysOgrDto(
  val ogrs31Year: BigDecimal? = null,
  val ogrs32Year: BigDecimal? = null,
  val ogrs3RiskRecon: String? = null
)
