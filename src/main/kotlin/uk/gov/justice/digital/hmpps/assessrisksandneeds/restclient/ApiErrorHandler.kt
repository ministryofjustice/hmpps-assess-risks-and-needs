package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient

import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.ClientResponse
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.ExternalApiAuthorisationException
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.ExternalApiDuplicateOffenderRecordException
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.ExternalApiEntityNotFoundException
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.ExternalApiForbiddenException
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.ExternalApiInvalidRequestException
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.ExternalApiUnknownException

fun handle4xxError(
  clientResponse: ClientResponse,
  method: HttpMethod,
  url: String,
  client: ExternalService
): Mono<out Throwable?>? {
  return when (clientResponse.statusCode()) {
    HttpStatus.BAD_REQUEST -> {
      clientResponse.bodyToMono(ApiErrorResponse::class.java)
        .map { error -> ExternalApiInvalidRequestException(error.developerMessage, method, url, client) }
    }
    HttpStatus.UNAUTHORIZED -> {
      clientResponse.bodyToMono(ApiErrorResponse::class.java)
        .map { error -> ExternalApiAuthorisationException(error.developerMessage, method, url, client) }
    }
    HttpStatus.FORBIDDEN -> {
      clientResponse.bodyToMono(ApiErrorResponse::class.java)
        .map { error -> ExternalApiForbiddenException(error.developerMessage, method, url, client) }
    }
    HttpStatus.NOT_FOUND -> {
      clientResponse.bodyToMono(ApiErrorResponse::class.java)
        .map { error -> ExternalApiEntityNotFoundException(error.developerMessage, method, url, client) }
    }
    HttpStatus.CONFLICT -> {
      clientResponse.bodyToMono(ApiErrorResponse::class.java)
        .map { error -> ExternalApiDuplicateOffenderRecordException(error.developerMessage, method, url, client) }
    }
    else -> handleError(clientResponse, method, url, client)
  }
}

fun handle5xxError(
  message: String,
  method: HttpMethod,
  path: String,
  service: ExternalService
): Mono<out Throwable?>? {
  throw ExternalApiUnknownException(
    message,
    method,
    path,
    service
  )
}

fun handleError(
  clientResponse: ClientResponse,
  method: HttpMethod,
  url: String,
  client: ExternalService
): Mono<out Throwable?>? {
  val httpStatus = clientResponse.statusCode()
  return clientResponse.bodyToMono(String::class.java).map { error ->
    ExternalApiUnknownException(error, method, url, client)
  }.or(
    Mono.error(
      ExternalApiUnknownException(
        "Unexpected exception with no body and status $httpStatus",
        method,
        url,
        client
      )
    )
  )
}
