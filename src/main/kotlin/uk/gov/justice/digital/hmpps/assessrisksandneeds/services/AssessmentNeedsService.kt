package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentNeedsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentStatus
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentSummary
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PersonIdentifier
import uk.gov.justice.digital.hmpps.assessrisksandneeds.config.Clock
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.OasysApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.isCompletedWithinTimeframe
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.isWithinTimeframe
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.EntityNotFoundException

@Service
class AssessmentNeedsService(private val oasysApiRestClient: OasysApiRestClient, private val clock: Clock) {
  fun getAssessmentNeeds(crn: String, timeframe: Long = 55, excludeIncomplete: Boolean = true): AssessmentNeedsDto {
    val latestAssessment = oasysApiRestClient.getLatestAssessment(
      PersonIdentifier(PersonIdentifier.Type.CRN, crn),
      needsPredicate(timeframe, excludeIncomplete, clock),
    ) ?: throw EntityNotFoundException("No needs found for CRN: $crn")

    val needs = oasysApiRestClient.getCriminogenicNeedsForAssessment(latestAssessment)
      ?: throw EntityNotFoundException("No needs found for CRN: $crn")

    val assessment = needs.assessments.firstOrNull { it.assessmentPk == latestAssessment.assessmentId }
      ?: needs.assessments.singleOrNull()?.also {
        log.warn(
          "Criminogenic needs for assessment {} (CRN {}) contained no matching assessmentPk; using the only assessment returned (pk={})",
          latestAssessment.assessmentId,
          crn,
          it.assessmentPk,
        )
      }
      ?: throw EntityNotFoundException("No needs found for CRN: $crn")

    return AssessmentNeedsDto.from(assessment)
  }

  companion object {
    private val log: Logger = LoggerFactory.getLogger(AssessmentNeedsService::class.java)
  }
}

fun needsPredicate(timeframe: Long, excludeIncomplete: Boolean = true, clock: Clock): (AssessmentSummary) -> Boolean = {
  listOf(
    it.assessmentType == AssessmentType.LAYER3.name,
    if (excludeIncomplete) it.status == AssessmentStatus.COMPLETE.name else true,
    if (excludeIncomplete) it.isCompletedWithinTimeframe(timeframe, clock) else it.isWithinTimeframe(timeframe, clock),
  ).all { ok -> ok }
}
