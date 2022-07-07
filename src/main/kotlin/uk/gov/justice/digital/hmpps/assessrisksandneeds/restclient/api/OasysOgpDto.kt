package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysOgpDto(
  val ogpStWesc: BigDecimal? = null,
  val ogpDyWesc: BigDecimal? = null,
  val ogpTotWesc: BigDecimal? = null,
  val ogp1Year: BigDecimal? = null,
  val ogp2Year: BigDecimal? = null,
  val ogpRisk: String? = null
)
