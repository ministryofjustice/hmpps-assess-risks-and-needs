package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

data class SupplementaryRiskDto(

  @Schema(description = "Supplementary Risk ID", example = "78beac68-884c-4784-9bea-fd8088f52a47")
  val supplementaryRiskId: UUID? = null,

  @Schema(description = "Source of Risk", example = "INTERVENTION_REFERRAL")
  val source: Source,

  @Schema(description = "Source Id", example = "78beac68-884c-4784-9bea-fd8088f52a47 or 1989823")
  val sourceId: String,

  @Schema(description = "Offender CRN", example = "DX12340A")
  val crn: String,

  @Schema(description = "Created By User", example = "Paul Newman")
  val createdByUser: String,

  @Schema(description = "Created By User Type", example = "delius")
  val createdByUserType: String,

  @Schema(description = "Created At", example = "")
  val createdDate: LocalDateTime = LocalDateTime.now(),

  @Schema(description = "Risk Summary Comments", example = "Free text up to 4000 characters")
  val riskSummaryComments: String,
)

enum class Source {
  INTERVENTION_REFERRAL;

  companion object {
    fun fromString(enumValue: String?): Source {
      return values().firstOrNull { it.name == enumValue }
        ?: throw IllegalArgumentException("Unknown Source $enumValue")
    }
  }
}

enum class UserType(val value: String) {
  DELIUS("delius"), AUTH("auth"), NOMIS("nomis");

  companion object {
    fun fromString(enumValue: String?): UserType {
      return values().firstOrNull { it.value == enumValue }
        ?: throw IllegalArgumentException("Unknown User Type $enumValue")
    }
  }
}
