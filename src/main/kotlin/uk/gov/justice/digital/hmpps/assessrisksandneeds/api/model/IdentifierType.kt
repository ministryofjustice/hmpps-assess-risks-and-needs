package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

enum class IdentifierType(val value: String) {
  CRN("crn"),
  ;

  companion object {
    fun fromString(enumValue: String?): IdentifierType = IdentifierType.values().firstOrNull { it.value.equals(enumValue, ignoreCase = true) }
      ?: throw IllegalArgumentException("Unknown Identifier Type $enumValue")
  }
}
