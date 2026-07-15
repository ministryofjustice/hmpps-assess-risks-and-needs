package uk.gov.justice.digital.hmpps.assessrisksandneeds.e2e

data class TokenDto(
  val access_token: String,
  val expires_in: Int,
  val token_type: String,
  val scope: String,
  val sub: String,
  val auth_source: String,
  val iss: String,
  val jti: String,
)
