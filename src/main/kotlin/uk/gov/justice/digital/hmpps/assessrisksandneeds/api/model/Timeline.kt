package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import com.fasterxml.jackson.annotation.JsonAlias
import java.time.LocalDateTime

data class Timeline(val timeline: List<BasicAssessmentSummary>)

interface AssessmentSummary {
  val assessmentId: Long
  val initiationDate: LocalDateTime
  val completedDate: LocalDateTime?
  val assessmentType: String
  val status: String
}

data class BasicAssessmentSummary(
  @JsonAlias("assessmentPk")
  override val assessmentId: Long,
  override val initiationDate: LocalDateTime,
  override val completedDate: LocalDateTime?,
  override val assessmentType: String,
  override val status: String,
) : AssessmentSummary

data class AssessmentSummaryWithSanIndicator(
  @JsonAlias("assessmentPk")
  override val assessmentId: Long,
  override val initiationDate: LocalDateTime,
  override val completedDate: LocalDateTime?,
  override val assessmentType: String,
  override val status: String,
  val sanIndicator: Boolean,
) : AssessmentSummary

fun AssessmentSummary.withSanIndicator(sanIndicator: Boolean) = AssessmentSummaryWithSanIndicator(
  assessmentId,
  initiationDate,
  completedDate,
  assessmentType,
  status,
  sanIndicator,
)
