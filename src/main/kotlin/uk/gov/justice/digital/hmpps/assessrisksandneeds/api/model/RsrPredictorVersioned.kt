package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.LocalDateTime

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "outputVersion",
  visible = true,
)
@JsonSubTypes(
  JsonSubTypes.Type(value = RsrPredictorVersionedLegacyDto::class, name = "1"),
  JsonSubTypes.Type(value = RsrPredictorVersionedDto::class, name = "2"),
)
sealed interface RsrPredictorVersioned<out T> {
  val completedDate: LocalDateTime?
  val source: RsrScoreSource
  val status: AssessmentStatus
  val outputVersion: String
  val output: T?
}
