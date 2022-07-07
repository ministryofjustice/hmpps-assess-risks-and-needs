package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysOspDto(
  val ospImagePercentageScore: BigDecimal? = null,
  val ospContactPercentageScore: BigDecimal? = null,
  val ospImageScoreLevel: String? = null,
  val ospContactScoreLevel: String? = null
)
