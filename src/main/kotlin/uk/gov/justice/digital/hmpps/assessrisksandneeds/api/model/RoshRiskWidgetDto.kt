package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskLevel.HIGH
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskLevel.LOW
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskLevel.MEDIUM
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskLevel.VERY_HIGH
import java.time.LocalDateTime

fun convertRiskLevelToString(riskLevel: RiskLevel?): String? {
  return when (riskLevel) {
    VERY_HIGH -> "VERY_HIGH"
    HIGH -> "HIGH"
    MEDIUM -> "MEDIUM"
    LOW -> "LOW"
    null -> null
  }
}

fun getRiskLevel(riskInCommunityDto: Map<RiskLevel?, List<String>>, risk: String): String? {
  return if (riskInCommunityDto[VERY_HIGH].orEmpty().contains(risk)) {
    convertRiskLevelToString(VERY_HIGH)
  } else if (riskInCommunityDto[HIGH].orEmpty().contains(risk)) {
    convertRiskLevelToString(HIGH)
  } else if (riskInCommunityDto[MEDIUM].orEmpty().contains(risk)) {
    convertRiskLevelToString(MEDIUM)
  } else if (riskInCommunityDto[LOW].orEmpty().contains(risk)) {
    convertRiskLevelToString(LOW)
  } else {
    null
  }
}

data class RoshRiskWidgetDto(
  @Schema(description = "Has a RoSH risk assessment been complete?", example = "true")
  val hasBeenCompleted: Boolean? = null,

  @Schema(description = "Overall ROSH risk score", example = "VERY_HIGH")
  val overallRisk: String? = null,

  @Schema(description = "Assessed on", example = "2021-10-10")
  val lastUpdated: LocalDateTime? = null,

  @Schema(description = "Risk to children in the community", example = "HIGH")
  val riskToChildrenInCommunity: String? = null,

  @Schema(description = "Risk to public in the community", example = "MEDIUM")
  val riskToPublicInCommunity: String? = null,

  @Schema(description = "Risk to known adult in the community", example = "LOW")
  val riskToKnownAdultInCommunity: String? = null,

  @Schema(description = "Risk to staff in the community", example = "VERY_HIGH")
  val riskToStaffInCommunity: String? = null,
) {
  companion object {
    fun from(riskSummary: RiskRoshSummaryDto): RoshRiskWidgetDto {
      return RoshRiskWidgetDto(
        hasBeenCompleted = true,
        overallRisk = convertRiskLevelToString(riskSummary.overallRiskLevel),
        lastUpdated = riskSummary.assessedOn,
        riskToChildrenInCommunity = getRiskLevel(riskSummary.riskInCommunity, "Children"),
        riskToPublicInCommunity = getRiskLevel(riskSummary.riskInCommunity, "Public"),
        riskToKnownAdultInCommunity = getRiskLevel(riskSummary.riskInCommunity, "Known Adult"),
        riskToStaffInCommunity = getRiskLevel(riskSummary.riskInCommunity, "Staff"),
      )
    }
  }
}
