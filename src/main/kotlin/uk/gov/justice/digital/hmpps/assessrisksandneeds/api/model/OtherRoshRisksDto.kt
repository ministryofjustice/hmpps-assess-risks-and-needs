package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import com.fasterxml.jackson.annotation.JsonView
import io.swagger.v3.oas.annotations.media.Schema

data class OtherRoshRisksDto(
  @Schema(description = "Escape / abscond")
  @JsonView(View.Probation::class)
  val escapeOrAbscond: RiskDto?,

  @Schema(description = "Control issues / disruptive behaviour")
  @JsonView(View.Probation::class)
  val controlIssuesDisruptiveBehaviour: RiskDto?,

  @Schema(description = "Concerns in respect of breach of trust")
  @JsonView(View.Probation::class)
  val breachOfTrust: RiskDto?
)
