package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class SupplementaryRiskDto(
  @Schema(description = "Source of Risk", example = "INTERVENTION_REFERRAL")
  val source: Source,

  @Schema(description = "Source Id", example = "78beac68-884c-4784-9bea-fd8088f52a47")
  val sourceId: UUID,

  @Schema(description = "Offender CRN", example = "DX12340A")
  val crn: String?,

  @Schema(description = "Source Id", example = "78beac68-884c-4784-9bea-fd8088f52a47")
  val userId: String?,

  @Schema(description = "Source Id", example = "Free text up to 4000 characters")
  val riskSummary: String?,
)

enum class Source {
  INTERVENTION_REFERRAL
}
