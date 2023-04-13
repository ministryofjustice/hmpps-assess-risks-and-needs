package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import java.math.BigDecimal

class OspScoreDto(
  val ospIndecentPercentageScore: BigDecimal? = null,
  val ospContactPercentageScore: BigDecimal? = null,
  val ospIndecentScoreLevel: ScoreLevel? = null,
  val ospContactScoreLevel: ScoreLevel? = null,
)
