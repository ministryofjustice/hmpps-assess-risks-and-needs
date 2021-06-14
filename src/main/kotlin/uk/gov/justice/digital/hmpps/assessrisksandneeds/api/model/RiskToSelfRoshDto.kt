package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import com.fasterxml.jackson.annotation.JsonView
import io.swagger.v3.oas.annotations.media.Schema

data class RiskToSelfRoshDto(

  @Schema(description = "Risk of suicide?")
  @JsonView(View.CrsProvider::class)
  val suicide: RiskDto?,

  @Schema(description = "Risk of self harm?")
  @JsonView(View.CrsProvider::class)
  val selfHarm: RiskDto?,

  @Schema(description = "Coping in custody")
  @JsonView(View.CrsProvider::class)
  val custody: RiskDto?,

  @Schema(description = "Coping in hostel setting")
  @JsonView(View.CrsProvider::class)
  val hotelSetting: RiskDto?,

  @Schema(description = "Vulnerability")
  @JsonView(View.CrsProvider::class)
  val vulnerability: RiskDto?,
)
