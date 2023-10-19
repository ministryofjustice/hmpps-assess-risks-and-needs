package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

data class CaseAccess(
  val crn: String,
  val userExcluded: Boolean,
  val userRestricted: Boolean,
  val exclusionMessage: String?,
  val restrictionMessage: String?,
)

data class UserAccess(val access: List<CaseAccess>)
