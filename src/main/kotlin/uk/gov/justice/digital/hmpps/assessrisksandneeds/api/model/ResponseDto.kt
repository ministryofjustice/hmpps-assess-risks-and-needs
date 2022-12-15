package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

enum class ResponseDto(val value: String) {
  YES("YES"), NO("NO"), DK("DON'T KNOW"), NA("N/A");

  companion object {
    fun fromString(enumValue: String?): ResponseDto? {
      return values().firstOrNull { it.value == enumValue?.uppercase() }
    }
  }
}
