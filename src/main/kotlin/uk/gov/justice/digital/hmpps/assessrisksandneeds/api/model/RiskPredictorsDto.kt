package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import com.fasterxml.jackson.annotation.JsonFormat
import java.math.BigDecimal
import java.time.LocalDateTime

data class RiskPredictorsDto(
  val algorithmVersion: String,
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  val calculatedAt: LocalDateTime,
  val type: PredictorType,
  val scoreType: ScoreType?,
  val scores: Map<PredictorSubType, Score>,
  val errors: List<String> = emptyList(),
  val errorCount: Int = 0,
)

data class Score(
  val level: ScoreLevel?,
  val score: BigDecimal?,
  val isValid: Boolean,
)

enum class ScoreLevel(val type: String) {
  LOW("Low"), MEDIUM("Medium"), HIGH("High"), VERY_HIGH("Very High"), NOT_APPLICABLE("Not Applicable");

  companion object {
    fun findByType(type: String?): ScoreLevel? {
      return values().firstOrNull { value -> value.type == type }
    }

    fun findByOrdinal(ordinal: Int?): ScoreLevel? {
      return when (ordinal) {
        1 -> LOW
        2 -> MEDIUM
        3 -> HIGH
        4 -> VERY_HIGH
        else -> null
      }
    }
  }
}

enum class PredictorType {
  RSR,
}

enum class ScoreType(val type: String) {
  STATIC("STATIC"), DYNAMIC("DYNAMIC");

  companion object {
    fun findByType(type: String): ScoreType? {
      return values().firstOrNull { value -> value.type.compareTo(type, true) == 0 }
    }
  }
}
