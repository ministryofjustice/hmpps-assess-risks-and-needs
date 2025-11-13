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
  JsonSubTypes.Type(value = AllPredictorVersionedLegacyDto::class, name = "1"),
  JsonSubTypes.Type(value = AllPredictorVersionedDto::class, name = "2"),
)
interface AllPredictorVersioned<out T> {
  val completedDate: LocalDateTime?
  val status: AssessmentStatus
  val version: Int?
  val output: T?
}
