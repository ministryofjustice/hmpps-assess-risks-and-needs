package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import com.fasterxml.jackson.annotation.JsonAlias
import java.time.LocalDateTime

data class Timeline(val timeline: List<AssessmentSummary>)

data class AssessmentSummary(
  @JsonAlias("assessmentPk")
  val assessmentId: Long,
  val completedDate: LocalDateTime?,
  val assessmentType: String,
  val status: String,
)

data class AssessmentSummaryIndicators(
  val assessments: List<AssessmentSummaryIndicator>,
)
data class AssessmentSummaryIndicator(
  val indicators: Indicators,
) {
  fun getSanIndicator(): Boolean {
    return indicators.sanIndicator == "Y"
  }
}

data class Indicators(
  val sanIndicator: String?,
)
