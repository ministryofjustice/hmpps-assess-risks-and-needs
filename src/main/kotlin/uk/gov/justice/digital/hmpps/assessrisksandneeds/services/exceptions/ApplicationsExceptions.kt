package uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions

import org.springframework.http.HttpMethod
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.SupplementaryRiskDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.ExternalService

class EntityNotFoundException(msg: String?) : RuntimeException(msg)
class DuplicateSourceRecordFound(msg: String?, val supplementaryRiskDto: SupplementaryRiskDto? = null) :
  RuntimeException(msg)

// External Services Exceptions
class ExternalApiEntityNotFoundException(
  msg: String,
  val method: HttpMethod,
  val url: String,
  val client: ExternalService
) : RuntimeException(msg)

class ExternalApiAuthorisationException(
  msg: String,
  val method: HttpMethod,
  val url: String,
  val client: ExternalService
) : RuntimeException(msg)

class ExternalApiForbiddenException(
  msg: String,
  val method: HttpMethod,
  val url: String,
  val client: ExternalService
) : RuntimeException(msg)

class ExternalApiInvalidRequestException(
  msg: String,
  val method: HttpMethod,
  val url: String,
  val client: ExternalService
) : RuntimeException(msg)

class ExternalApiUnknownException(
  msg: String,
  val method: HttpMethod,
  val url: String,
  val client: ExternalService
) : RuntimeException(msg)
