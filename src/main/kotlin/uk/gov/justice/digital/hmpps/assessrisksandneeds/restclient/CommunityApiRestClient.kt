package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient

import org.aspectj.weaver.tools.cache.SimpleCacheFactory.path
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.CaseAccess
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.UserAccess
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.ExternalApiEntityNotFoundException
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.ExternalApiForbiddenException

@Component
class CommunityApiRestClient(

  @Qualifier("communityApiWebClient")
  val webClient: AuthenticatingRestClient,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun verifyUserAccess(crn: String, deliusUsername: String): CaseAccess? {
    log.info("Client retrieving LAO details for crn: $crn")
    return webClient
      .post(listOf(crn)) {
        it.path("/users/access")
        it.queryParam("username", deliusUsername)
      }
      .retrieve()
      .onStatus(
        { it == HttpStatus.NOT_FOUND },
        {
          it.releaseBody()
            .then(
              Mono.fromCallable {
                ExternalApiEntityNotFoundException(
                  "No such user for username: $deliusUsername",
                  HttpMethod.GET,
                  path,
                  ExternalService.COMMUNITY_API,
                )
              },
            )
        },
      )
      .onStatus({ it.is4xxClientError }) {
        handle4xxError(
          it,
          HttpMethod.GET,
          path,
          ExternalService.COMMUNITY_API,
        )
      }
      .onStatus({ it.is5xxServerError }) {
        handle5xxError(
          "Failed to retrieve LAO details for crn: $crn",
          HttpMethod.GET,
          path,
          ExternalService.COMMUNITY_API,
        )
      }
      .bodyToMono<UserAccess>()
      .mapNotNull { u -> u.access.firstOrNull { it.crn == crn } }
      .handle { access, sink ->
        if (access == null) {
          sink.error(
            ExternalApiEntityNotFoundException(
              "No such offender for CRN: $crn",
              HttpMethod.GET,
              path,
              ExternalService.COMMUNITY_API,
            ),
          )
        } else if (access.userExcluded || access.userRestricted) {
          sink.error(
            ExternalApiForbiddenException(
              "User does not have permission to access offender with CRN $crn.",
              HttpMethod.GET,
              path,
              ExternalService.COMMUNITY_API,
              listOfNotNull(access.exclusionMessage, access.restrictionMessage),
            ),
          )
        } else {
          sink.next(access)
        }
      }
      .switchIfEmpty(
        Mono.error(
          ExternalApiEntityNotFoundException(
            "No such offender for CRN: $crn",
            HttpMethod.GET,
            path,
            ExternalService.COMMUNITY_API,
          ),
        ),
      )
      .block()
  }
}
