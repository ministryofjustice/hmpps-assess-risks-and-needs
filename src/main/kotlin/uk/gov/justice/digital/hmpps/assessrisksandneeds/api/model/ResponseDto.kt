package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

enum class ResponseDto(val value: String) {
  YES("Yes"), NO("No"), DONTKNOW("Don't know");

  companion object {
    fun fromString(enumValue: String?): ResponseDto {
      return values().firstOrNull { it.value == enumValue }
        ?: throw IllegalArgumentException("Unknown Responsee $enumValue")
    }
  }
}
