package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient

data class ApiErrorResponse(
  val status: Int? = 0,
  val developerMessage: String? = null,
)
