package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import com.fasterxml.jackson.annotation.JsonView
import java.time.LocalDateTime

data class AllRoshRiskDto(
  @JsonView(View.AllRisksView::class)
  val riskToSelf: RoshRiskToSelfDto,

  @JsonView(View.AllRisksView::class)
  val otherRisks: OtherRoshRisksDto,

  @JsonView(View.AllRisksView::class)
  val summary: RiskRoshSummaryDto,

  @JsonView(View.AllRisksView::class)
  val assessedOn: LocalDateTime?,
)
