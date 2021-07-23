package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import java.math.BigDecimal


data class RiskPredictorsDto(
  val rsrScore: Score
)

data class Score(
  val type: PredictorType,
  val score: ScoreLevel,
  val scoreNumeric: BigDecimal
)

enum class ScoreLevel {
  LOW, MEDIUM, HIGH
}

enum class PredictorType {
  RSR
}
