package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

enum class UserType(val value: String) {
  DELIUS("delius"),
  AUTH("auth"),
  NOMIS("nomis"),
  ;

  companion object {
    fun fromString(enumValue: String?): UserType = values().firstOrNull { it.value == enumValue }
      ?: throw IllegalArgumentException("Unknown User Type $enumValue")
  }
}
