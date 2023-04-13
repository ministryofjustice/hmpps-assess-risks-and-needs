package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

enum class PredictorSubType {
  RSR, OSPC, OSPI, SNSV,
  RSR_1YR_BRIEF,
  RSR_2YR_BRIEF,
  RSR_1YR_EXTENDED,
  RSR_2YR_EXTENDED,
  OSPI_1YR,
  OSPI_2YR,
  OSPC_1YR,
  OSPC_2YR,
  SNSV_1YR_BRIEF,
  SNSV_2YR_BRIEF,
  SNSV_1YR_EXTENDED,
  SNSV_2YR_EXTENDED,
  ;

  companion object {
    fun fromString(enumValue: String?): PredictorSubType {
      return values().firstOrNull { it.name == enumValue }
        ?: throw IllegalArgumentException("Unknown PredictorSubType $enumValue")
    }
  }
}
