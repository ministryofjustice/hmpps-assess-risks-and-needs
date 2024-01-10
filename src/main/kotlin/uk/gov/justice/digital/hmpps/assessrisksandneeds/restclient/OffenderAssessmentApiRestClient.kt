package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient

import org.aspectj.weaver.tools.cache.SimpleCacheFactory.path
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.bodyToMono
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OffenderNeedsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PersonIdentifier
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.Timeline
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.SectionAnswersDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.SectionCodesDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.SectionHeader

@Component
class OffenderAssessmentApiRestClient {
  @Autowired
  @Qualifier("offenderAssessmentApiWebClient")
  internal lateinit var webClient: AuthenticatingRestClient

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun getRoshSectionsForCompletedLastYearAssessment(
    crn: String,
  ): SectionAnswersDto? {
    log.info("Retrieving Rosh sections for last year completed Assessment for crn $crn")
    val path =
      "/assessments/crn/$crn/sections/answers?assessmentStatus=COMPLETE&assessmentTypes=LAYER_1,LAYER_3&period=YEAR&periodUnits=1"
    return webClient
      .post(
        path,
        SectionCodesDto(
          setOf(
            SectionHeader.ROSH_SCREENING,
            SectionHeader.ROSH_FULL_ANALYSIS,
            SectionHeader.ROSH_SUMMARY,
          ),
        ),
      )
      .retrieve()
      .onStatus({ it.is4xxClientError }) {
        log.error(
          "4xx Error retrieving Rosh sections for last year completed Assessment for crn $crn code: " +
            "${it.statusCode().value()}",
        )
        handle4xxError(
          it,
          HttpMethod.POST,
          path,
          ExternalService.ASSESSMENTS_API,
        )
      }
      .onStatus({ it.is5xxServerError }) {
        log.error(
          "5xx Error retrieving Rosh sections for last year completed Assessment for crn $crn code: " +
            "${it.statusCode().value()}",
        )
        handle5xxError(
          "Failed to retrieve Rosh sections for last year completed Assessment for crn $crn",
          HttpMethod.POST,
          path,
          ExternalService.ASSESSMENTS_API,
        )
      }
      .bodyToMono(SectionAnswersDto::class.java)
      .block().also { log.info("Retrieved Rosh sections for last year completed Assessment for crn $crn") }
  }

  fun getNeedsForCompletedLastYearAssessment(
    crn: String,
  ): OffenderNeedsDto? {
    log.info("Retrieving needs for last year completed Assessment for crn $crn")
    val path =
      "/assessments/crn/$crn/needs/latest?period=YEAR&periodUnits=1"
    return webClient
      .get(
        path,
      )
      .retrieve()
      .onStatus({ it.is4xxClientError }) {
        log.error("4xx Error retrieving needs for last year completed Assessment for crn $crn code: ${it.statusCode().value()}")
        handle4xxError(
          it,
          HttpMethod.GET,
          path,
          ExternalService.ASSESSMENTS_API,
        )
      }
      .onStatus({ it.is5xxServerError }) {
        log.error("5xx Error retrieving needs for last year completed Assessment for crn $crn code: ${it.statusCode().value()}")
        handle5xxError(
          "Failed to retrieve needs for last year completed Assessment for crn $crn",
          HttpMethod.POST,
          path,
          ExternalService.ASSESSMENTS_API,
        )
      }
      .bodyToMono(OffenderNeedsDto::class.java)
      .block().also { log.info("Retrieved needs for last year completed Assessment for crn $crn") }
  }

  fun getAssessmentTimeline(
    identifier: PersonIdentifier,
  ): Timeline? {
    return webClient
      .get {
        it.path("/assessments/timeline/${identifier.value}/ALLOW")
          .queryParam("identityType", identifier.type.value)
      }
      .retrieve()
      .onStatus({ it.is4xxClientError }) {
        log.error("4xx Error retrieving assessment timeline for $identifier code: ${it.statusCode().value()}")
        handle4xxError(
          it,
          HttpMethod.GET,
          path,
          ExternalService.ASSESSMENTS_API,
        )
      }
      .onStatus({ it.is5xxServerError }) {
        log.error("5xx Error retrieving assessment timeline for $identifier code: ${it.statusCode().value()}")
        handle5xxError(
          "Failed to retrieve assessment timeline for $identifier",
          HttpMethod.GET,
          path,
          ExternalService.ASSESSMENTS_API,
        )
      }
      .bodyToMono<Timeline>()
      .block().also { log.info("Retrieved assessment timeline for $identifier") }
  }
}
