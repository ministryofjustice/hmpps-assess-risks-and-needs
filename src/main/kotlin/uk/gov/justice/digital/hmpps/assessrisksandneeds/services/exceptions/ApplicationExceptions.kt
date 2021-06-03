package uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions

// Internal Service Exceptions
class UserNotAuthorisedException(msg: String?) : RuntimeException(msg)
class EntityNotFoundException(msg: String?) : RuntimeException(msg)