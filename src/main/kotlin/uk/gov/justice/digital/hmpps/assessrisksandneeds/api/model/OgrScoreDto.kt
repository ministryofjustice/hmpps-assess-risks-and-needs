package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import java.math.BigDecimal

class OgrScoreDto(
  val oneYear: BigDecimal? = null,
  val twoYears: BigDecimal? = null,
  val scoreLevel: ScoreLevel? = null
)
