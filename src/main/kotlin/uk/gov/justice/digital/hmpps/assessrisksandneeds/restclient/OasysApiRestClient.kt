package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysAssessmentOffenceDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysRiskManagementPlanDetailsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysRiskPredictorsDto

@Component
class OasysApiRestClient {
  @Autowired
  @Qualifier("oasysApiWebClient")
  internal lateinit var webClient: AuthenticatingRestClient

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun getRiskPredictorsForCompletedAssessments(
    crn: String,
  ): OasysRiskPredictorsDto? {
    val path = "/ass/allrisk/$crn/ALLOW"
    return webClient
      .get(
        path,
      )
      .retrieve()
      .onStatus({ it.is4xxClientError }) {
        log.error("4xx Error retrieving risk predictor scores for completed Assessments for crn $crn code: ${it.statusCode().value()}")
        handle4xxError(
          it,
          HttpMethod.GET,
          path,
          ExternalService.ASSESSMENTS_API,
        )
      }
      .onStatus({ it.is5xxServerError }) {
        log.error("5xx Error retrieving risk predictor scores for completed Assessments for crn $crn code: ${it.statusCode().value()}")
        handle5xxError(
          "Failed to retrieve risk predictor scores for completed Assessments for crn $crn",
          HttpMethod.GET,
          path,
          ExternalService.ASSESSMENTS_API,
        )
      }
      .bodyToMono(OasysRiskPredictorsDto::class.java)
      .block().also { log.info("Retrieved risk predictor scores for completed Assessments for crn $crn") }
  }

  fun getAssessmentTimeline(
    crn: String,
  ): String? {
    val path = "/ass/timeline/$crn/ALLOW"
    return webClient
      .get(
        path,
      )
      .retrieve()
      .onStatus({ it.is4xxClientError }) {
        log.error("4xx Error retrieving assessment timeline for crn $crn code: ${it.statusCode().value()}")
        handle4xxError(
          it,
          HttpMethod.GET,
          path,
          ExternalService.ASSESSMENTS_API,
        )
      }
      .onStatus({ it.is5xxServerError }) {
        log.error("5xx Error retrieving assessment timeline for crn $crn code: ${it.statusCode().value()}")
        handle5xxError(
          "Failed to retrieve assessment timeline for crn: $crn",
          HttpMethod.GET,
          path,
          ExternalService.ASSESSMENTS_API,
        )
      }
      .bodyToMono(String::class.java)
      .block().also { log.info("Retrieved assessment timeline for crn $crn") }
  }

  fun getAssessmentOffence(
    crn: String,
    limitedAccessOffender: String,
  ): OasysAssessmentOffenceDto? {
    val path = "/ass/offence/$crn/$limitedAccessOffender"
    return webClient
      .get(
        path,
      )
      .retrieve()
      .onStatus({ it.is4xxClientError }) {
        log.error("4xx Error retrieving assessment offence for crn $crn code: ${it.statusCode().value()}")
        handle4xxError(
          it,
          HttpMethod.GET,
          path,
          ExternalService.ASSESSMENTS_API,
        )
      }
      .onStatus({ it.is5xxServerError }) {
        log.error("5xx Error retrieving assessment offence for crn $crn code: ${it.statusCode().value()}")
        handle5xxError(
          "Failed to retrieve assessment offence for crn $crn",
          HttpMethod.POST,
          path,
          ExternalService.ASSESSMENTS_API,
        )
      }
      .bodyToMono(OasysAssessmentOffenceDto::class.java)
      .block().also { log.info("Retrieved assessment offence for crn $crn") }
  }

  fun getRiskManagementPlan(crn: String, limitedAccessOffender: String): OasysRiskManagementPlanDetailsDto? {
    val path = "/ass/rmp/$crn/$limitedAccessOffender"
    return webClient
      .get(
        path,
      )
      .retrieve()
      .onStatus({ it.is4xxClientError }) {
        log.error("4xx Error retrieving risk management plan for crn $crn code: ${it.statusCode().value()}")
        handle4xxError(
          it,
          HttpMethod.GET,
          path,
          ExternalService.ASSESSMENTS_API,
        )
      }
      .onStatus({ it.is5xxServerError }) {
        log.error("5xx Error retrieving risk management plan for crn $crn code: ${it.statusCode().value()}")
        handle5xxError(
          "Failed to retrieve risk management plan for crn $crn",
          HttpMethod.GET,
          path,
          ExternalService.ASSESSMENTS_API,
        )
      }
      .bodyToMono(OasysRiskManagementPlanDetailsDto::class.java)
      .block().also { log.info("Retrieved risk management plan for crn $crn") }
  }
}
