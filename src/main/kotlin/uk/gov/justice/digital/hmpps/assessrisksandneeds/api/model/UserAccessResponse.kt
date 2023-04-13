package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

data class UserAccessResponse(
  val exclusionMessage: String?,
  val restrictionMessage: String?,
  val userExcluded: Boolean,
  val userRestricted: Boolean,
)
