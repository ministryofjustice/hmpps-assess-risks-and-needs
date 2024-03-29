package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.controllers.advice

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageConversionException
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.SupplementaryRiskDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.DuplicateSourceRecordFound
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.EntityNotFoundException
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.ExternalApiAuthorisationException
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.ExternalApiDuplicateOffenderRecordException
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.ExternalApiEntityNotFoundException
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.ExternalApiForbiddenException
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.ExternalApiInvalidRequestException
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.ExternalApiUnknownException
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.IncorrectInputParametersException
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.UserNameNotFoundException

@ControllerAdvice
class ControllerAdvice {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @ExceptionHandler(EntityNotFoundException::class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  fun handle(e: EntityNotFoundException): ResponseEntity<ErrorResponse?> {
    log.info("EntityNotFoundException: ", e)
    return ResponseEntity(ErrorResponse(status = 404, developerMessage = e.message), HttpStatus.NOT_FOUND)
  }

  @ExceptionHandler(IncorrectInputParametersException::class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  fun handle(e: IncorrectInputParametersException): ResponseEntity<ErrorResponse?> {
    log.info("IncorrectInputParametersException: ", e)
    return ResponseEntity(ErrorResponse(status = 400, developerMessage = e.message), HttpStatus.BAD_REQUEST)
  }

  @ExceptionHandler(MethodArgumentNotValidException::class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  fun handle(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse?> {
    log.info("MethodArgumentNotValidException: ", e)
    return ResponseEntity(ErrorResponse(status = 400, developerMessage = e.message), HttpStatus.BAD_REQUEST)
  }

  @ExceptionHandler(HttpMessageConversionException::class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  fun handle(e: HttpMessageConversionException): ResponseEntity<*> {
    log.error("HttpMessageConversionException: ", e)
    return ResponseEntity(ErrorResponse(status = 400, developerMessage = e.message), HttpStatus.BAD_REQUEST)
  }

  @ExceptionHandler(HttpMessageNotReadableException::class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  fun handle(e: HttpMessageNotReadableException): ResponseEntity<ErrorResponse?> {
    log.error("HttpMessageNotReadableException: ", e)
    return ResponseEntity(ErrorResponse(status = 400, developerMessage = e.message), HttpStatus.BAD_REQUEST)
  }

  @ExceptionHandler(UserNameNotFoundException::class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  fun handle(e: UserNameNotFoundException): ResponseEntity<ErrorResponse?> {
    log.error("UserNameNotFoundException: ", e)
    return ResponseEntity(ErrorResponse(status = 400, developerMessage = e.message), HttpStatus.BAD_REQUEST)
  }

  @ExceptionHandler(value = [org.springframework.security.access.AccessDeniedException::class])
  @ResponseStatus(HttpStatus.FORBIDDEN)
  fun handle(e: org.springframework.security.access.AccessDeniedException): ResponseEntity<ErrorResponse?> {
    log.error("AccessDeniedException: ", e)
    return ResponseEntity(ErrorResponse(status = 403, developerMessage = e.message), HttpStatus.FORBIDDEN)
  }

  @ExceptionHandler(DuplicateSourceRecordFound::class)
  @ResponseStatus(HttpStatus.CONFLICT)
  fun handle(e: DuplicateSourceRecordFound): ResponseEntity<SupplementaryRiskDto?> {
    log.error("DuplicateSourceRecordFound: ", e)
    return ResponseEntity(e.supplementaryRiskDto, HttpStatus.CONFLICT)
  }

  @ExceptionHandler(ExternalApiEntityNotFoundException::class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  fun handle(e: ExternalApiEntityNotFoundException): ResponseEntity<ErrorResponse?> {
    log.warn("ApiClientEntityNotFoundException for external client ${e.client} method ${e.method} and url ${e.url}: ", e)
    return ResponseEntity(ErrorResponse(status = 404, developerMessage = e.message), HttpStatus.NOT_FOUND)
  }

  @ExceptionHandler(ExternalApiUnknownException::class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  fun handle(e: ExternalApiUnknownException): ResponseEntity<ErrorResponse?> {
    log.error("ExternalApiUnknownException for external client ${e.client} method ${e.method} and url ${e.url}: ", e)
    return ResponseEntity(ErrorResponse(status = 500, developerMessage = e.message), HttpStatus.INTERNAL_SERVER_ERROR)
  }

  @ExceptionHandler(ExternalApiInvalidRequestException::class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  fun handle(e: ExternalApiInvalidRequestException): ResponseEntity<ErrorResponse?> {
    log.error("InvalidRequestException for external client ${e.client} method ${e.method} and url ${e.url}: ", e)
    return ResponseEntity(ErrorResponse(status = 400, developerMessage = e.message), HttpStatus.BAD_REQUEST)
  }

  @ExceptionHandler(ExternalApiAuthorisationException::class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  fun handle(e: ExternalApiAuthorisationException): ResponseEntity<ErrorResponse?> {
    log.error("ApiClientAuthorisationException for external client ${e.client} method ${e.method} and url ${e.url}: ", e)
    return ResponseEntity(ErrorResponse(status = 401, developerMessage = e.message), HttpStatus.UNAUTHORIZED)
  }

  @ExceptionHandler(ExternalApiForbiddenException::class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  fun handle(e: ExternalApiForbiddenException): ResponseEntity<ErrorResponse?> {
    log.error(
      "ApiForbiddenException for external client ${e.client} method ${e.method} and url ${e.url}: ",
      e,
    )
    return ResponseEntity(ErrorResponse(status = 403, developerMessage = e.message, moreInfo = e.moreInfo.joinToString { "," }), HttpStatus.FORBIDDEN)
  }

  @ExceptionHandler(ExternalApiDuplicateOffenderRecordException::class)
  fun handle(e: ExternalApiDuplicateOffenderRecordException): ResponseEntity<ErrorResponse?> {
    log.error("DuplicateOffenderRecordException: ", e)
    return ResponseEntity(ErrorResponse(status = 409, developerMessage = e.message, userMessage = e.message), HttpStatus.CONFLICT)
  }

  @ExceptionHandler(Exception::class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  fun handle(e: Exception): ResponseEntity<ErrorResponse?> {
    log.error("Exception: ", e)
    return ResponseEntity(
      ErrorResponse(
        status = 500,
        developerMessage = "Internal Server Error. Check Logs",
        userMessage = "An unexpected error has occurred",
      ),
      HttpStatus.INTERNAL_SERVER_ERROR,
    )
  }
}
