package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import com.fasterxml.jackson.annotation.JsonView
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class RoshRiskToSelfDto(

  @Schema(description = "Risk of suicide?")
  @JsonView(View.CrsProvider::class, View.RiskView::class)
  val suicide: RiskDto? = null,

  @Schema(description = "Risk of self harm?")
  @JsonView(View.CrsProvider::class, View.RiskView::class)
  val selfHarm: RiskDto? = null,

  @Schema(description = "Coping in custody")
  @JsonView(View.CrsProvider::class, View.RiskView::class)
  val custody: RiskDto? = null,

  @Schema(description = "Coping in hostel setting")
  @JsonView(View.CrsProvider::class, View.RiskView::class)
  val hostelSetting: RiskDto? = null,

  @Schema(description = "Vulnerability")
  @JsonView(View.CrsProvider::class, View.RiskView::class)
  val vulnerability: RiskDto? = null,

  @Schema(description = "The date and time that the assessment was completed")
  @JsonView(View.CrsProvider::class, View.SingleRisksView::class)
  val assessedOn: LocalDateTime? = null,
)
