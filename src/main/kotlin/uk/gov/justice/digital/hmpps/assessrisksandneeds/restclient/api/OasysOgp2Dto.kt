package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysOgp2Dto(
  val ogp2Yr2: BigDecimal? = null,
  val ogp2Band: String? = null,
  val ogp2Calculated: String? = null,
)
