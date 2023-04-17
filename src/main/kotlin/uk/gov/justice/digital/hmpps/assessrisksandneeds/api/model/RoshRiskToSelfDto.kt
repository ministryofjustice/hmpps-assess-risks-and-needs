package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import com.fasterxml.jackson.annotation.JsonView
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class RoshRiskToSelfDto(

  @Schema(description = "Risk of suicide?")
  @JsonView(View.CrsProvider::class, View.RiskView::class)
  val suicide: RiskDto?,

  @Schema(description = "Risk of self harm?")
  @JsonView(View.CrsProvider::class, View.RiskView::class)
  val selfHarm: RiskDto?,

  @Schema(description = "Coping in custody")
  @JsonView(View.CrsProvider::class, View.RiskView::class)
  val custody: RiskDto?,

  @Schema(description = "Coping in hostel setting")
  @JsonView(View.CrsProvider::class, View.RiskView::class)
  val hostelSetting: RiskDto?,

  @Schema(description = "Vulnerability")
  @JsonView(View.CrsProvider::class, View.RiskView::class)
  val vulnerability: RiskDto?,

  @Schema(description = "The date and time that the assessment was completed")
  @JsonView(View.CrsProvider::class, View.SingleRisksView::class)
  val assessedOn: LocalDateTime?,
)
