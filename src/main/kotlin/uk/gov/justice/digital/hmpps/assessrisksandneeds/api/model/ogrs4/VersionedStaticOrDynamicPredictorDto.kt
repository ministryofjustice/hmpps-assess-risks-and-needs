package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ogrs4

import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreType
import java.math.BigDecimal

class VersionedStaticOrDynamicPredictorDto(
  val algorithmVersion: String? = null,
  staticOrDynamic: ScoreType? = null,
  score: BigDecimal? = null,
  band: ScoreLevel? = null,
) : StaticOrDynamicPredictorDto(staticOrDynamic, score, band)
