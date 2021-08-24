package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

enum class PredictorSubType {
  RSR, OSPC, OSPI;

  companion object {
    fun fromString(enumValue: String?): PredictorSubType {
      return values().firstOrNull { it.name == enumValue }
        ?: throw IllegalArgumentException("Unknown PredictorSubType $enumValue")
    }
  }
}
