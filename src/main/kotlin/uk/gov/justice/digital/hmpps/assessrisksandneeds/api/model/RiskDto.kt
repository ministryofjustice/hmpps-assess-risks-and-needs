package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import io.swagger.v3.oas.annotations.media.Schema

data class RiskDto(
  @Schema(description = "is there any risk", example = "Yes, No, Don't know, null")
  val risk: ResponseDto? = null,
  @Schema(description = "Previous concerns", example = "Yes, No, Don't know")
  val previous: ResponseDto? = null,
  @Schema(description = "Previous concerns supporting comments", example = "Risk of self harms concerns due to ...")
  val previousConcernsText: String? = null,
  @Schema(description = "Current concerns", example = "Yes, No, Don't know")
  val current: ResponseDto? = null,
  @Schema(description = "Current concerns supporting comments", example = "Risk of self harms concerns due to ...")
  val currentConcernsText: String? = null,
)
