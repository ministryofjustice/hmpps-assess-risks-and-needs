package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysSnsvDto(
  val snsvStaticYr2: BigDecimal? = null,
  val snsvDynamicYr2: BigDecimal? = null,
  val snsvStaticYr2Band: String? = null,
  val snsvDynamicYr2Band: String? = null,
  val snsvStaticCalculated: String? = null,
  val snsvDynamicCalculated: String? = null,
)
