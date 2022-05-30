package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import java.math.BigDecimal

class RsrScoreDto(
  val percentageScore: BigDecimal? = null,
  val staticOrDynamic: ScoreType? = null,
  val source: RsrScoreSource,
  val algorithmVersion: String? = null,
  val scoreLevel: ScoreLevel? = null
)
