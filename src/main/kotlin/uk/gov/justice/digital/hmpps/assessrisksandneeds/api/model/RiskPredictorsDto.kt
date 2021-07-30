package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import java.math.BigDecimal

data class RiskPredictorsDto(
  val algorithmVersion: Int,
  val type: PredictorType,
  val scoreType: ScoreType?,
  val rsrScore: Score,
  val ospcScore: Score,
  val ospiScore: Score
)

data class Score(
  val level: ScoreLevel?,
  val score: BigDecimal?,
  val isValid: Boolean
)

enum class ScoreLevel(val type: String) {
  LOW("Low"), MEDIUM("Medium"), HIGH("High"), NOT_APPLICABLE("Not Applicable");

  companion object {
    fun findByType(type: String): ScoreLevel? {
      return values().firstOrNull { value -> value.type == type }
    }
  }
}

enum class PredictorType {
  RSR
}

enum class ScoreType(val type: String) {
  STATIC("Static"), DYNAMIC("Dynamic");

  companion object {
    fun findByType(type: String): ScoreType? {
      return values().firstOrNull { value -> value.type == type }
    }
  }
}
