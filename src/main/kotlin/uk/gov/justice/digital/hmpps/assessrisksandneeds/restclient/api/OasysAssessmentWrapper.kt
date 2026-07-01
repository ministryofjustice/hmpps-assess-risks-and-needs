package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysAssessmentWrapper<T : Any>(
  @param:JsonProperty("probNumber")
  val crn: String? = null,
  val assessments: List<T> = emptyList(),
)
