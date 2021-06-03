package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

data class SupplementaryRiskDto(

  @Schema(description = "Supplementary Risk ID", example = "78beac68-884c-4784-9bea-fd8088f52a47")
  val supplementaryRiskId: UUID = UUID.randomUUID(),

  @Schema(description = "Source of Risk", example = "INTERVENTION_REFERRAL")
  val source: Source,

  @Schema(description = "Source Id", example = "78beac68-884c-4784-9bea-fd8088f52a47 or 1989823")
  val sourceId: String? = null,

  @Schema(description = "Offender CRN", example = "DX12340A")
  val crn: String? = null,

  @Schema(description = "Created By User", example = "Paul Newman")
  val createdByUser: String? = null,

  @Schema(description = "Created By User Type", example = "DELIUS")
  val createdByUserType: UserType? = null,

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

enum class UserType {
  DELIUS, INTERVENTIONS_PROVIDER;

  companion object {
    fun fromString(enumValue: String?): UserType {
      return UserType.values().firstOrNull { it.name == enumValue }
        ?: throw IllegalArgumentException("Unknown User Type $enumValue")
    }
  }
}
