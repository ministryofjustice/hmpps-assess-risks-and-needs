package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ogrs4

import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreLevel
import java.math.BigDecimal

open class BasePredictorDto(
  val score: BigDecimal? = null,
  val band: ScoreLevel? = null,
)
