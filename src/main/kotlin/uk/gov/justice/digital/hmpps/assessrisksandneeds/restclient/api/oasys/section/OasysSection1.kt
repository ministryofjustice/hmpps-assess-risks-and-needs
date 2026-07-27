package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.oasys.section

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

data class OasysSection1(
  @param:JsonProperty("everCommittedSexualOffence")
  val everCommittedSexualOffenceRaw: String?, // question 1.30

  @param:JsonProperty("assessor")
  val assessor: Assessor? = null,

  @param:JsonProperty("countersigner")
  val countersigner: Assessor? = null,
) {
  @get:JsonIgnore
  val everCommittedSexualOffence: Boolean? = everCommittedSexualOffenceRaw.yesNo()

  @get:JsonIgnore
  val assessorName: String? get() = assessor?.name

  @get:JsonIgnore
  val countersignerName: String? get() = countersigner?.name

  fun String?.yesNo() = when (this?.trim()?.lowercase()) {
    "yes" -> true
    "no" -> false
    else -> null
  }
}

data class Assessor(
  @param:JsonProperty("name")
  val name: String? = null,
)
