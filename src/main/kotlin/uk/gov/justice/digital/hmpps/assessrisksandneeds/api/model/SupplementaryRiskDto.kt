package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class SupplementaryRiskDto(

  @Schema(description = "Supplementary Risk ID", example = "78beac68-884c-4784-9bea-fd8088f52a47")
  val supplementaryRiskId: UUID?,

  @Schema(description = "Source of Risk", example = "INTERVENTION_REFERRAL")
  val source: Source,

  @Schema(description = "Source Id", example = "78beac68-884c-4784-9bea-fd8088f52a47")
  val sourceId: String,

  @Schema(description = "Offender CRN", example = "DX12340A")
  val crn: String?,

  @Schema(description = "User Id", example = "78beac68-884c-4784-9bea-fd8088f52a47")
  val userId: String?,

  @Schema(description = "User Type", example = "DELIUS")
  val userType: UserType?,

  @Schema(description = "Risk Summary Comments", example = "Free text up to 4000 characters")
  val riskSummaryComments: String?,
)

enum class Source {
  INTERVENTION_REFERRAL
}

enum class UserType {
  DELIUS, INTERVENTIONS_PROVIDER
}
