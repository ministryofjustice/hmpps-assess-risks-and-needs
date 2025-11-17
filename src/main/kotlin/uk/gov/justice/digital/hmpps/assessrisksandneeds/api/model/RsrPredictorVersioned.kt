package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.LocalDateTime

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "version",
)
@JsonSubTypes(
  JsonSubTypes.Type(value = RsrPredictorVersionedLegacyDto::class, name = "1"),
  JsonSubTypes.Type(value = RsrPredictorVersionedDto::class, name = "2"),
)
sealed interface RsrPredictorVersioned<out T> {
  val calculatedDate: LocalDateTime?
  val completedDate: LocalDateTime?
  val signedDate: LocalDateTime?
  val source: RsrScoreSource
  val status: AssessmentStatus
  val version: Int?
  val output: T?
}
