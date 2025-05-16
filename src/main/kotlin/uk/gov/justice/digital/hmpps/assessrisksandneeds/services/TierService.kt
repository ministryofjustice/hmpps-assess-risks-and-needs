package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentStatus
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentSummary
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PersonIdentifier
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.withSanIndicator
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.OasysApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.SectionSummary
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.isWithin55Weeks

@Service
class TierService(private val ordsApiClient: OasysApiRestClient) {

  fun getSectionsForTier(personIdentifier: PersonIdentifier, sections: List<NeedsSection>): SectionSummary? = ordsApiClient.getLatestAssessment(personIdentifier, tierPredicate())?.let {
    val sanIndicator = ordsApiClient.getAssessmentSummaryIndicators(it, personIdentifier.value)
      .assessments.first().getSanIndicator()
    ordsApiClient.getScoredSectionsForAssessment(it.withSanIndicator(sanIndicator), sections)
  }
}

private fun tierPredicate(): (AssessmentSummary) -> Boolean = {
  it.assessmentType == AssessmentType.LAYER3.name &&
    it.status in listOf(AssessmentStatus.COMPLETE.name, AssessmentStatus.LOCKED_INCOMPLETE.name) &&
    it.isWithin55Weeks()
}
