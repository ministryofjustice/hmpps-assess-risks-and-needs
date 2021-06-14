package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

data class AllRoshRiskDto(
  val riskToSelf: RoshRiskToSelfDto,
  val otherRisks: OtherRoshRisksDto,
  val summary: RiskRoshSummaryDto
)
