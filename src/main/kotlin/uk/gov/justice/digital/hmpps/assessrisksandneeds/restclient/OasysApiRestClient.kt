package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AllRoshRiskDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentSummary
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PersonIdentifier
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskRoshSummaryDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.Timeline
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysAssessmentOffenceDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysRiskManagementPlanDetailsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysRiskPredictorsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.RoshContainer
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.RoshFull
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.RoshScreening
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.RoshSummary
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.oasys.section.ScoredSection
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.NeedsSection
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.SectionHeader
import java.time.LocalDate

@Component
class OasysApiRestClient(
  @Qualifier("oasysApiWebClient") private val webClient: AuthenticatingRestClient,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun getLatestAssessment(identifier: PersonIdentifier): AssessmentSummary? =
    getAssessmentTimeline(identifier)?.timeline
      ?.filter { it.completedDate != null }
      ?.sortedByDescending { it.completedDate }?.firstOrNull()

  fun getScoredSections(
    identifier: PersonIdentifier,
    needsSection: List<NeedsSection>,
  ): Map<NeedsSection, ScoredSection> =
    getLatestAssessment(identifier)?.let {
      Flux.fromIterable(needsSection).flatMap { section ->
        val path = "/ass/section${section.sectionNumber}/ALLOW/${it.assessmentId}"
        webClient
          .get(path)
          .exchangeToMono { ScoredSectionProvider.mapSection(section)(it) }
      }.collectList().block()?.toMap()
    } ?: mapOf()

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
    identifier: PersonIdentifier,
  ): Timeline? {
    val path = "/ass/allasslist/${identifier.type.ordsUrlParam}/${identifier.value}/ALLOW"
    return webClient
      .get(
        path,
      )
      .retrieve()
      .onStatus({ it.is4xxClientError }) {
        log.error("4xx Error retrieving assessment timeline for ${identifier.type.value} ${identifier.value} code: ${it.statusCode().value()}")
        handle4xxError(
          it,
          HttpMethod.GET,
          path,
          ExternalService.ASSESSMENTS_API,
        )
      }
      .onStatus({ it.is5xxServerError }) {
        log.error("5xx Error retrieving assessment timeline for ${identifier.type.value} ${identifier.value} code: ${it.statusCode().value()}")
        handle5xxError(
          "Failed to retrieve assessment timeline for ${identifier.type.value} ${identifier.value}",
          HttpMethod.GET,
          path,
          ExternalService.ASSESSMENTS_API,
        )
      }
      .bodyToMono<Timeline>()
      .block().also { log.info("Retrieved assessment timeline for ${identifier.type.value} ${identifier.value}") }
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

  fun getRoshSummary(identifier: PersonIdentifier): RiskRoshSummaryDto? {
    val latestCompleted =
      getAssessmentTimeline(identifier)?.timeline
        ?.filter { it.completedDate != null }
        ?.sortedByDescending { it.completedDate }?.firstOrNull()
    return latestCompleted?.takeIf {
      it.completedDate?.toLocalDate()?.isBefore(LocalDate.now().minusWeeks(55)) == false
    }?.let { assessment ->
      getRoshSummary(assessment.assessmentId).map { it.asRiskRoshSummary() }.block()
    }
  }

  fun getRoshDetailForLatestCompletedAssessment(identifier: PersonIdentifier): AllRoshRiskDto? {
    val latestCompleted =
      getAssessmentTimeline(identifier)?.timeline
        ?.filter { it.completedDate != null }
        ?.sortedByDescending { it.completedDate }?.firstOrNull()
    return latestCompleted
      ?.takeIf {
        it.completedDate?.toLocalDate()?.isBefore(LocalDate.now().minusWeeks(55)) == false
      }
      ?.let { assessment ->
        getRoshFull(assessment.assessmentId)
          .zipWith(getRoshScreening(assessment.assessmentId))
          .zipWith(getRoshSummary(assessment.assessmentId))
          .map {
            AllRoshRiskDto(
              riskToSelf = it.t1.t1.asRiskToSelf(it.t1.t2),
              otherRisks = it.t1.t1.asOtherRisks(),
              summary = it.t2.asRiskRoshSummary(),
              assessedOn = assessment.completedDate,
            )
          }.block()
      }
  }

  private fun getRoshSummary(assessmentId: Long): Mono<RoshSummary> {
    val path = "/ass/section${SectionHeader.ROSH_SUMMARY.ordsUrlParam}/ALLOW/$assessmentId"
    return webClient
      .get(path)
      .exchangeToMono {
        if (it.statusCode() == HttpStatus.OK) {
          it.bodyToMono<RoshContainer<RoshSummary>>().map { c -> c.assessments.firstOrNull() ?: RoshSummary() }
        } else {
          Mono.just(RoshSummary())
        }
      }
  }

  private fun getRoshFull(assessmentId: Long): Mono<RoshFull> {
    val path = "/ass/section${SectionHeader.ROSH_FULL_ANALYSIS.ordsUrlParam}/ALLOW/$assessmentId"
    return webClient
      .get(path)
      .exchangeToMono {
        if (it.statusCode() == HttpStatus.OK) {
          it.bodyToMono<RoshContainer<RoshFull>>().map { c -> c.assessments.firstOrNull() ?: RoshFull() }
        } else {
          Mono.just(RoshFull())
        }
      }
  }

  private fun getRoshScreening(assessmentId: Long): Mono<RoshScreening> {
    val path = "/ass/section${SectionHeader.ROSH_SCREENING.ordsUrlParam}/ALLOW/$assessmentId"
    return webClient
      .get(path)
      .exchangeToMono {
        if (it.statusCode() == HttpStatus.OK) {
          it.bodyToMono<RoshContainer<RoshScreening>>().map { c -> c.assessments.firstOrNull() ?: RoshScreening() }
        } else {
          Mono.just(RoshScreening())
        }
      }
  }
}
