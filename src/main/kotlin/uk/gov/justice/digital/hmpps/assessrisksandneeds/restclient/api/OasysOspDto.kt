package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysOspDto(
  val ospImagePercentageScore: BigDecimal? = null,
  val ospContactPercentageScore: BigDecimal? = null,
  val ospImageScoreLevel: String? = null,
  val ospContactScoreLevel: String? = null,
  val ospIndirectImagesChildrenPercentageScore: BigDecimal? = null,
  val ospDirectContactPercentageScore: BigDecimal? = null,
  val ospIndirectImagesChildrenScoreLevel: String? = null,
  val ospDirectContactScoreLevel: String? = null,
)
