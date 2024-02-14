package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import com.fasterxml.jackson.annotation.JsonView
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class OtherRoshRisksDto(
  @Schema(description = "Escape / abscond")
  @JsonView(View.Hmpps::class, View.RiskView::class)
  val escapeOrAbscond: ResponseDto? = null,

  @Schema(description = "Control issues / disruptive behaviour")
  @JsonView(View.Hmpps::class, View.RiskView::class)
  val controlIssuesDisruptiveBehaviour: ResponseDto? = null,

  @Schema(description = "Concerns in respect of breach of trust")
  @JsonView(View.Hmpps::class, View.RiskView::class)
  val breachOfTrust: ResponseDto? = null,

  @Schema(description = "Risks to other prisoners")
  @JsonView(View.Hmpps::class, View.RiskView::class)
  val riskToOtherPrisoners: ResponseDto? = null,

  @Schema(description = "The date and time that the assessment was completed")
  @JsonView(View.Hmpps::class, View.SingleRisksView::class)
  val assessedOn: LocalDateTime? = null,
)
