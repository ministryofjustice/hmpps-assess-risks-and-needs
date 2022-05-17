package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.RefElementDto
import java.math.BigDecimal

class OgpScoreDto(
  val ogpStaticWeightedScore: BigDecimal? = null,
  val ogpDynamicWeightedScore: BigDecimal? = null,
  val ogpTotalWeightedScore: BigDecimal? = null,
  val ogp1Year: BigDecimal? = null,
  val ogp2Year: BigDecimal? = null,
  val ogpRisk: RefElementDto? = null
) {

}
