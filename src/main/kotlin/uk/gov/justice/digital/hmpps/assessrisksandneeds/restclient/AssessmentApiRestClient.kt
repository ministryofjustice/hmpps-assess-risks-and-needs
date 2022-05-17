package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.CurrentOffenceDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.CurrentOffencesDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.DynamicScoringOffencesDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OffenderAndOffencesDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OffenderNeedsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PredictorType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PreviousOffencesDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.CurrentOffence
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.CurrentOffences
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.DynamicScoringOffences
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysPredictorsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysRSRPredictorsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OffenderAndOffencesBodyDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.PreviousOffences
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.SectionAnswersDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.SectionCodesDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.SectionHeader
import java.time.temporal.ChronoUnit.YEARS

@Component
// offender assessments api
class AssessmentApiRestClient {
  @Autowired
  @Qualifier("assessmentApiWebClient")
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
            SectionHeader.ROSH_SUMMARY
          )
        )
      )
      .retrieve()
      .onStatus(HttpStatus::is4xxClientError) {
        log.error(
          "4xx Error retrieving Rosh sections for last year completed Assessment for crn $crn code: ${
          it.statusCode().value()
          }"
        )
        handle4xxError(
          it,
          HttpMethod.POST,
          path,
          ExternalService.ASSESSMENTS_API
        )
      }
      .onStatus(HttpStatus::is5xxServerError) {
        log.error(
          "5xx Error retrieving Rosh sections for last year completed Assessment for crn $crn code: ${
          it.statusCode().value()
          }"
        )
        handle5xxError(
          "Failed to retrieve Rosh sections for last year completed Assessment for crn $crn",
          HttpMethod.POST,
          path,
          ExternalService.ASSESSMENTS_API
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
        path
      )
      .retrieve()
      .onStatus(HttpStatus::is4xxClientError) {
        log.error("4xx Error retrieving needs for last year completed Assessment for crn $crn code: ${it.statusCode().value()}")
        handle4xxError(
          it,
          HttpMethod.GET,
          path,
          ExternalService.ASSESSMENTS_API
        )
      }
      .onStatus(HttpStatus::is5xxServerError) {
        log.error("5xx Error retrieving needs for last year completed Assessment for crn $crn code: ${it.statusCode().value()}")
        handle5xxError(
          "Failed to retrieve needs for last year completed Assessment for crn $crn",
          HttpMethod.POST,
          path,
          ExternalService.ASSESSMENTS_API
        )
      }
      .bodyToMono(OffenderNeedsDto::class.java)
      .block().also { log.info("Retrieved needs for last year completed Assessment for crn $crn") }
  }

  fun calculatePredictorTypeScoring(
    predictorType: PredictorType,
    offenderAndOffences: OffenderAndOffencesDto,
    algorithmVersion: String? = null
  ): OasysRSRPredictorsDto? {
    log.info("Calculating $predictorType scoring for offender with crn ${offenderAndOffences.crn}")
    val path =
      "/offenders/risks/predictors/$predictorType"
    val body = offenderAndOffences.toOffenderAndOffencesBodyDto(algorithmVersion)
    log.info("Calculating $predictorType scoring with $body")
    return webClient
      .post(
        path,
        body
      )
      .retrieve()
      .onStatus(HttpStatus::is4xxClientError) {
        log.error(
          "4xx Error calculating $predictorType scoring for offender with crn ${offenderAndOffences.crn} code: ${
          it.statusCode().value()
          }"
        )
        handle4xxError(
          it,
          HttpMethod.POST,
          path,
          ExternalService.ASSESSMENTS_API
        )
      }
      .onStatus(HttpStatus::is5xxServerError) {
        log.error(
          "5xx Error calculating $predictorType scoring for offender with crn ${offenderAndOffences.crn} code: ${
          it.statusCode().value()
          }"
        )
        handle5xxError(
          "Failed to calculate $predictorType scoring for offender with crn ${offenderAndOffences.crn}",
          HttpMethod.POST,
          path,
          ExternalService.ASSESSMENTS_API
        )
      }
      .bodyToMono(OasysRSRPredictorsDto::class.java)
      .block()
      .also { log.info("Retrieved calculated $predictorType scoring for offender with crn ${offenderAndOffences.crn}") }
  }

  fun getPredictorScoresForOffender(
    crn: String,
  ): List<OasysPredictorsDto>? {
    log.info("Retrieving Predictor scores for crn $crn")
    val path = "/offenders/crn/$crn/predictors"
    return webClient
      .get(
        path
      )
      .retrieve()
      .onStatus(HttpStatus::is4xxClientError) {
        log.error(
          "4xx Error retrieving Predictor scores for crn $crn code: ${
          it.statusCode().value()
          }"
        )
        handle4xxError(
          it,
          HttpMethod.GET,
          path,
          ExternalService.ASSESSMENTS_API
        )
      }
      .onStatus(HttpStatus::is5xxServerError) {
        log.error(
          "5xx Error retrieving Predictor scores for crn $crn code: ${
          it.statusCode().value()
          }"
        )
        handle5xxError(
          "Failed to retrieve Predictor scores for crn $crn",
          HttpMethod.POST,
          path,
          ExternalService.ASSESSMENTS_API
        )
      }
      .bodyToMono(object : ParameterizedTypeReference<List<OasysPredictorsDto>>() {})
      .block().also { log.info("Retrieved Predictor scores for crn $crn") }
  }

  fun getRiskScoresForCompletedLastYearAssessments(
    crn: String,
  ): List<OasysPredictorsDto>? {
    log.info("Retrieving risk predictor scores for last year completed Assessments for crn $crn")
    val path =
      "/offenders/crn/$crn/predictors/latest?period=YEAR&periodUnits=1"
    return webClient
      .get(
        path
      )
      .retrieve()
      .onStatus(HttpStatus::is4xxClientError) {
        log.error("4xx Error retrieving risk predictor scores for last year completed Assessments for crn $crn code: ${it.statusCode().value()}")
        handle4xxError(
          it,
          HttpMethod.GET,
          path,
          ExternalService.ASSESSMENTS_API
        )
      }
      .onStatus(HttpStatus::is5xxServerError) {
        log.error("5xx Error retrieving risk predictor scores for last year completed Assessments for crn $crn code: ${it.statusCode().value()}")
        handle5xxError(
          "Failed to retrieve risk predictor scores for last year completed Assessments for crn $crn",
          HttpMethod.GET,
          path,
          ExternalService.ASSESSMENTS_API
        )
      }
      .bodyToMono(object : ParameterizedTypeReference<List<OasysPredictorsDto>>() {})
      .block().also { log.info("Retrieved risk predictor scores for last year completed Assessments for crn $crn") }
  }
  
  private fun OffenderAndOffencesDto.toOffenderAndOffencesBodyDto(algorithmVersion: String?): OffenderAndOffencesBodyDto {
    return OffenderAndOffencesBodyDto(
      algorithmVersion = algorithmVersion,
      gender = this.gender.value,
      dob = this.dob,
      assessmentDate = this.assessmentDate,
      currentOffence = this.currentOffence.toCurrentOffenceBody(),
      dateOfFirstSanction = this.dateOfFirstSanction,
      ageAtFirstSanction = YEARS.between(this.dob, this.dateOfFirstSanction).toInt(),
      totalOffences = this.totalOffences,
      totalViolentOffences = this.totalViolentOffences,
      dateOfCurrentConviction = this.dateOfCurrentConviction,
      hasAnySexualOffences = this.hasAnySexualOffences,
      isCurrentSexualOffence = this.isCurrentSexualOffence,
      isCurrentOffenceVictimStranger = this.isCurrentOffenceVictimStranger,
      mostRecentSexualOffenceDate = this.mostRecentSexualOffenceDate,
      totalSexualOffencesInvolvingAnAdult = this.totalSexualOffencesInvolvingAnAdult,
      totalSexualOffencesInvolvingAChild = this.totalSexualOffencesInvolvingAChild,
      totalSexualOffencesInvolvingChildImages = this.totalSexualOffencesInvolvingChildImages,
      totalNonContactSexualOffences = this.totalNonContactSexualOffences,
      earliestReleaseDate = this.earliestReleaseDate,
      dynamicScoringOffences = this.hasCompletedInterview.let { this.dynamicScoringOffences?.toDynamicScoringOffencesBody(this.hasCompletedInterview) }
    )
  }

  fun CurrentOffenceDto.toCurrentOffenceBody(): CurrentOffence {
    return CurrentOffence(offenceCode, offenceSubcode)
  }

  fun DynamicScoringOffencesDto.toDynamicScoringOffencesBody(hasCompletedInterview: Boolean): DynamicScoringOffences {
    return DynamicScoringOffences(
      hasCompletedInterview,
      this.hasSuitableAccommodation?.score,
      this.employment?.score,
      this.currentRelationshipWithPartner?.score,
      this.evidenceOfDomesticViolence,
      this.isPerpetrator,
      this.alcoholUseIssues?.score,
      this.bingeDrinkingIssues?.score,
      this.impulsivityIssues?.score,
      this.temperControlIssues?.score,
      this.proCriminalAttitudes?.score,
      this.previousOffences?.toPreviousOffencesBody(),
      this.currentOffences?.toCurrentOffencesBody()
    )
  }

  private fun CurrentOffencesDto.toCurrentOffencesBody(): CurrentOffences {
    return CurrentOffences(
      this.firearmPossession,
      this.offencesWithWeapon
    )
  }

  fun PreviousOffencesDto.toPreviousOffencesBody(): PreviousOffences {
    return PreviousOffences(
      this.murderAttempt,
      this.wounding,
      this.aggravatedBurglary,
      this.arson,
      this.criminalDamage,
      this.kidnapping,
      this.firearmPossession,
      this.robbery,
      this.offencesWithWeapon
    )
  }
}
