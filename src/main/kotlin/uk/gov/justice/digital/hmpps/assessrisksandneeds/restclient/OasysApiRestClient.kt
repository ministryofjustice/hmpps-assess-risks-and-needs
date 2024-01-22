package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Flux
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentSummary
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PersonIdentifier
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.Timeline
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysAssessmentOffenceDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysRiskManagementPlanDetailsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysRiskPredictorsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.oasys.section.ScoredSection
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.NeedsSection
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
      ?.filter {
        it.assessmentType == "LAYER3" &&
          (it.status == "COMPLETE" || it.status == "LOCKED_INCOMPLETE") &&
          it.completedDate != null
      }?.sortedByDescending { it.completedDate }?.firstOrNull()

  fun getScoredSections(
    identifier: PersonIdentifier,
    needsSection: List<NeedsSection>,
  ): TierAnswers {
    val assessment = getLatestAssessment(identifier)?.takeIf {
      it.completedDate?.toLocalDate()?.isBefore(LocalDate.now().minusWeeks(55)) == false
    }
    val needs = assessment?.let {
      Flux.fromIterable(needsSection).flatMap { section ->
        val path = "/ass/section${section.sectionNumber}/ALLOW/${it.assessmentId}"
        webClient
          .get(path)
          .exchangeToMono { ScoredSectionProvider.mapSection(section)(it) }
      }.collectList().block()?.toMap()
    } ?: mapOf()

    return needs.let {
      TierAnswers(
        assessment,
        it.section(NeedsSection.ACCOMMODATION),
        it.section(NeedsSection.EDUCATION_TRAINING_EMPLOYMENT),
        it.section(NeedsSection.RELATIONSHIPS),
        it.section(NeedsSection.LIFESTYLE),
        it.section(NeedsSection.DRUG_MISUSE),
        it.section(NeedsSection.ALCOHOL_MISUSE),
        it.section(NeedsSection.THINKING_AND_BEHAVIOUR),
        it.section(NeedsSection.ATTITUDE),
      )
    }
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
}

inline fun <reified T : ScoredSection> Map<NeedsSection, ScoredSection>.section(section: NeedsSection): T? =
  this[section] as T?

data class TierAnswers(
  val assessment: AssessmentSummary?,
  val accommodation: ScoredSection.Accommodation?,
  val educationTrainingEmployment: ScoredSection.EducationTrainingEmployment?,
  val relationships: ScoredSection.Relationships?,
  val lifestyleAndAssociates: ScoredSection.LifestyleAndAssociates?,
  val drugMisuse: ScoredSection.DrugMisuse?,
  val alcoholMisuse: ScoredSection.AlcoholMisuse?,
  val thinkingAndBehaviour: ScoredSection.ThinkingAndBehaviour?,
  val attitudes: ScoredSection.Attitudes?,
)
