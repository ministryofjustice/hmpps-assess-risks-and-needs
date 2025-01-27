package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

data class AssessmentSummaryIndicators(
  val assessments: List<AssessmentSummaryIndicator>,
)

data class AssessmentSummaryIndicator(
  val indicators: Indicators,
) {
  fun getSanIndicator(): Boolean = indicators.sanIndicator == "Y"
}

data class Indicators(
  val sanIndicator: String?,
)
