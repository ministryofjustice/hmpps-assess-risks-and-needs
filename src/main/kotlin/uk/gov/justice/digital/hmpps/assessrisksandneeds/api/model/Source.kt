package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

enum class Source {
  INTERVENTION_REFERRAL;

  companion object {
    fun fromString(enumValue: String?): Source {
      return values().firstOrNull { it.name == enumValue }
        ?: throw IllegalArgumentException("Unknown Source $enumValue")
    }
  }
}
