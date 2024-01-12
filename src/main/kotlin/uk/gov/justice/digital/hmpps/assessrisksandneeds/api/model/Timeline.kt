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
