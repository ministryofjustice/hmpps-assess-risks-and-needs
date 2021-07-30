package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.CurrentOffence
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.DynamicScoringOffences
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OffenderAndOffencesDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PredictorType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PreviousOffences
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.SectionHeader
import java.time.temporal.ChronoUnit.YEARS

@Component
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

  fun calculatePredictorTypeScoring(
    predictorType: PredictorType,
    offenderAndOffences: OffenderAndOffencesDto
  ): OasysRSRPredictorsDto? {

    log.info("Calculating $predictorType scoring for offender with crn ${offenderAndOffences.crn}")
    val path =
      "/offenders/risks/predictors/$predictorType"
    return webClient
      .post(
        path,
        offenderAndOffences.toOffenderAndOffencesBodyDto()
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

  private fun OffenderAndOffencesDto.toOffenderAndOffencesBodyDto(): OffenderAndOffencesBodyDto {
    return OffenderAndOffencesBodyDto(
      this.gender.name,
      this.dob,
      this.assessmentDate,
      this.currentOffence.toCurrentOffenceDto(),
      this.dateOfFirstSanction,
      YEARS.between(this.dob, this.dateOfFirstSanction).toInt(),
      this.totalOffences,
      this.totalViolentOffences,
      this.dateOfCurrentConviction,
      this.hasAnySexualOffences,
      this.isCurrentSexualOffence,
      this.isCurrentOffenceVictimStranger,
      this.mostRecentSexualOffenceDate,
      this.totalSexualOffencesInvolvingAnAdult,
      this.totalSexualOffencesInvolvingAChild,
      this.totalSexualOffencesInvolvingChildImages,
      this.totalNonSexualOffences,
      this.earliestReleaseDate,
      this.hasCompletedInterview.let { this.dynamicScoringOffences?.toDynamicScoringOffencesBody(this.hasCompletedInterview) }
    )
  }

  fun CurrentOffence.toCurrentOffenceDto(): CurrentOffenceBody {
    return CurrentOffenceBody(this.offenceCode, this.offenceSubcode)
  }

  fun DynamicScoringOffences.toDynamicScoringOffencesBody(hasCompletedInterview: Boolean): DynamicScoringOffencesBody {
    return DynamicScoringOffencesBody(
      hasCompletedInterview,
      this.committedOffenceUsingWeapon,
      this.hasSuitableAccommodation?.score,
      this.isUnemployed?.score,
      this.currentRelationshipWithPartner?.score,
      this.evidenceOfDomesticViolence,
      this.isAVictim,
      this.isAPerpetrator,
      this.alcoholUseIssues?.score,
      this.bingeDrinkingIssues?.score,
      this.impulsivityIssues?.score,
      this.temperControlIssues?.score,
      this.proCriminalAttitudes?.score,
      this.previousOffences?.toPreviousOffencesBody()
    )
  }

  fun PreviousOffences.toPreviousOffencesBody(): PreviousOffencesBody {
    return PreviousOffencesBody(
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
