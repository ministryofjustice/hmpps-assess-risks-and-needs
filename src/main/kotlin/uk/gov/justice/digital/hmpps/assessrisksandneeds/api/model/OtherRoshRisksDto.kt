package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import com.fasterxml.jackson.annotation.JsonView
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class OtherRoshRisksDto(
  @Schema(description = "Escape / abscond")
  @JsonView(View.Probation::class)
  val escapeOrAbscond: ResponseDto?,

  @Schema(description = "Control issues / disruptive behaviour")
  @JsonView(View.Probation::class)
  val controlIssuesDisruptiveBehaviour: ResponseDto?,

  @Schema(description = "Concerns in respect of breach of trust")
  @JsonView(View.Probation::class)
  val breachOfTrust: ResponseDto?,

  @Schema(description = "Risks to other prisoners")
  @JsonView(View.Probation::class)
  val riskToOtherPrisoners: ResponseDto?,

  @Schema(description = "The date and time that the assessment was completed")
  @JsonView(View.Probation::class)
  val assessedOn: LocalDateTime?
)
