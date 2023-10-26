package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

enum class ScoreLevel(val type: String) {
  LOW("Low"), MEDIUM("Medium"), HIGH("High"), VERY_HIGH("Very High"), NOT_APPLICABLE("Not Applicable");

  companion object {
    fun findByType(type: String?): ScoreLevel? {
      return values().firstOrNull { value -> value.type == type }
    }
  }
}

enum class PredictorType {
  RSR,
}

enum class ScoreType(val type: String) {
  STATIC("STATIC"), DYNAMIC("DYNAMIC");
}
