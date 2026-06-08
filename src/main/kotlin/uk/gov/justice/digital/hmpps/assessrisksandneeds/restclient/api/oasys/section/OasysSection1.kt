package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.oasys.section

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

data class OasysSection1(
  @param:JsonProperty("everCommittedSexualOffence")
  val everCommittedSexualOffenceRaw: String?, // question 1.30
) {
  @get:JsonIgnore
  val everCommittedSexualOffence: Boolean? = everCommittedSexualOffenceRaw.yesNo()

  fun String?.yesNo() = when (this?.trim()?.lowercase()) {
    "yes" -> true
    "no" -> false
    else -> null
  }
}
