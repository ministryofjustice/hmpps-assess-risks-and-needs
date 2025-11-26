package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ogrs4

import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreType
import java.math.BigDecimal

open class StaticOrDynamicPredictorDto(
  val staticOrDynamic: ScoreType? = null,
  score: BigDecimal? = null,
  band: ScoreLevel? = null,
) : BasePredictorDto(score, band)
