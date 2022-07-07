package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysOvpDto(
  val ovpStWesc: BigDecimal? = null,
  val ovpDyWesc: BigDecimal? = null,
  val ovpTotWesc: BigDecimal? = null,
  val ovp1Year: BigDecimal? = null,
  val ovp2Year: BigDecimal? = null,
  val ovpRisk: String? = null
)
