package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class CreateSupplementaryRiskDto(

  @Schema(description = "Source of Risk", example = "INTERVENTION_REFERRAL")
  val source: Source,

  @Schema(description = "Source Id", example = "78beac68-884c-4784-9bea-fd8088f52a47 or 1989823")
  val sourceId: String,

  @Schema(description = "Offender CRN", example = "DX12340A")
  val crn: String,

  @Schema(description = "Created By User Type", example = "delius")
  val createdByUserType: String,

  @Schema(description = "Created At", example = "")
  val createdDate: LocalDateTime = LocalDateTime.now(),

  @Schema(description = "Risk Summary Comments", example = "Free text up to 4000 characters")
  val riskSummaryComments: String,
)
