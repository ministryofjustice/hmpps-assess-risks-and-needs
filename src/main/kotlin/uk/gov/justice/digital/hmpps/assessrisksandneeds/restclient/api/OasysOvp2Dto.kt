package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysOvp2Dto(
  val ovp2Yr2: BigDecimal? = null,
  val ovp2Band: String? = null,
  val ovp2Calculated: String? = null,
)
