package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient

data class ApiErrorResponse(
  val status: String,
  val developerMessage: String
)
