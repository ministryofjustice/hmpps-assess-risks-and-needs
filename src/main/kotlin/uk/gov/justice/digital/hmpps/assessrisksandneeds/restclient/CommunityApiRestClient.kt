package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.UserAccessResponse
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.ExternalApiEntityNotFoundException
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.ExternalApiForbiddenException

@Component
class CommunityApiRestClient(

  @Qualifier("communityApiWebClient")
  val webClient: AuthenticatingRestClient
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun verifyUserAccess(crn: String, deliusUsername: String): UserAccessResponse? {
    log.info("Client retrieving LAO details for crn: $crn")
    val path = "/secure/offenders/crn/$crn/user/$deliusUsername/userAccess"
    return webClient
      .get(path)
      .retrieve()
      .onStatus({ it == HttpStatus.NOT_FOUND }, {
        it.bodyToMono(String::class.java)
          .switchIfEmpty(
            Mono.error(
              ExternalApiEntityNotFoundException(
                "No such offender for CRN: $crn",
                HttpMethod.GET,
                path,
                ExternalService.COMMUNITY_API
              )
            )
          )
          .map { error ->
            ExternalApiEntityNotFoundException(
              "No such user for username: $deliusUsername",
              HttpMethod.GET,
              path,
              ExternalService.COMMUNITY_API
            )
          }
      })
      .onStatus({ it == HttpStatus.FORBIDDEN }, {
        it.bodyToMono(UserAccessResponse::class.java)
          .map { error ->
            ExternalApiForbiddenException(
              "User does not have permission to access offender with CRN $crn.",
              HttpMethod.GET,
              path,
              ExternalService.COMMUNITY_API,
              listOfNotNull(error.exclusionMessage, error.restrictionMessage)
            )
          }
      })
      .onStatus(HttpStatus::is4xxClientError) {
        handle4xxError(
          it,
          HttpMethod.GET,
          path,
          ExternalService.COMMUNITY_API
        )
      }
      .onStatus(HttpStatus::is5xxServerError) {
        handle5xxError(
          "Failed to retrieve LAO details for crn: $crn",
          HttpMethod.GET,
          path,
          ExternalService.COMMUNITY_API
        )
      }
      .bodyToMono(UserAccessResponse::class.java)
      .block()
  }
}
