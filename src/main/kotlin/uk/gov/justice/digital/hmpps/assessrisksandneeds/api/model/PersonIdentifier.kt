package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

data class PersonIdentifier(
  val type: Type,
  val value: String,
) {
  enum class Type(val value: String, val ordsUrlParam: String) {
    CRN("crn", "prob"),
    NOMS("nomisId", "pris"),
    ;

    companion object {
      fun of(type: String) = entries.firstOrNull { it.value.equals(type, true) }
        ?: throw IllegalArgumentException("Unsupported Identifier Type Provided: $type")
    }
  }

  companion object {
    fun from(type: String, value: String) = PersonIdentifier(Type.of(type), value)
  }
}
