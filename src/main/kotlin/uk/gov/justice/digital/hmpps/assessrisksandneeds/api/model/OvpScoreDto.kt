package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import java.math.BigDecimal

class OvpScoreDto(
  val ovpStaticWeightedScore: BigDecimal? = null,
  val ovpDynamicWeightedScore: BigDecimal? = null,
  val ovpTotalWeightedScore: BigDecimal? = null,
  val oneYear: BigDecimal? = null,
  val twoYears: BigDecimal? = null,
  val ovpRisk: ScoreLevel? = null
) {

}
