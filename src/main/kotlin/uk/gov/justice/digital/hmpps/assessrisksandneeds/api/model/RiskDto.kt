package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import io.swagger.v3.oas.annotations.media.Schema

data class RiskDto(
  @Schema(description = "is there any risk", example = "Yes, No, Don't know, null")
  val risk: ResponseDto? = null,
  @Schema(description = "Previous concerns", example = "Risk of self harms concerns due to ...")
  val previous: ResponseDto? = null,
  @Schema(description = "Current concerns", example = "Risk of self harms concerns due to ...")
  val current: ResponseDto? = null,
)
