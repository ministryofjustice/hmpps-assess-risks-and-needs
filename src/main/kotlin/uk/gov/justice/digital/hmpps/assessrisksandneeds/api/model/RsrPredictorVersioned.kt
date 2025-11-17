package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import java.time.LocalDateTime

interface RsrPredictorVersioned<out T> {
  val calculatedDate: LocalDateTime?
  val completedDate: LocalDateTime?
  val signedDate: LocalDateTime?
  val source: RsrScoreSource
  val status: AssessmentStatus
  val version: Int?
  val output: T?
}