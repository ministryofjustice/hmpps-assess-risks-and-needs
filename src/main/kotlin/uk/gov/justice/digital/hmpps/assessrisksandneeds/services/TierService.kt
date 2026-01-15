package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentStatus
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentSummary
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PersonIdentifier
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.withSanIndicator
import uk.gov.justice.digital.hmpps.assessrisksandneeds.config.Clock
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.OasysApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.SectionSummary
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.isCompletedWithinTimeframe

@Service
class TierService(private val ordsApiClient: OasysApiRestClient, private val clock: Clock) {

  fun getSectionsForTier(personIdentifier: PersonIdentifier, sections: List<NeedsSection>): SectionSummary? = ordsApiClient.getLatestAssessment(personIdentifier, tierPredicate(clock))?.let {
    val sanIndicator = ordsApiClient.getAssessmentSummaryIndicators(it, personIdentifier.value)
      .assessments.first().getSanIndicator()
    ordsApiClient.getScoredSectionsForAssessment(it.withSanIndicator(sanIndicator), sections)
  }
}

private fun tierPredicate(clock: Clock): (AssessmentSummary) -> Boolean = {
  it.assessmentType == AssessmentType.LAYER3.name &&
    it.status in listOf(AssessmentStatus.COMPLETE.name, AssessmentStatus.LOCKED_INCOMPLETE.name) &&
    it.isCompletedWithinTimeframe(55, clock)
}
