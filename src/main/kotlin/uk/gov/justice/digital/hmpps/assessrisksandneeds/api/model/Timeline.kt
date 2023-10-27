package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import java.time.LocalDateTime

data class Timeline(val timeline: List<AssessmentSummary>)

data class AssessmentSummary(
  val completedDate: LocalDateTime,
  val assessmentType: String,
  val status: String,
)
