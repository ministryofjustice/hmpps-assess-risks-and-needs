package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentNeedDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentNeedsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentStatus
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentSummary
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PersonIdentifier
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.OasysApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.SectionSummary
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.oasys.section.ScoredAnswer
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.isWithin55Weeks
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.EntityNotFoundException

@Service
class AssessmentNeedsService(private val oasysApiRestClient: OasysApiRestClient) {
  fun getAssessmentNeeds(crn: String): AssessmentNeedsDto {
    val sectionSummary = oasysApiRestClient.getLatestAssessment(
      PersonIdentifier(PersonIdentifier.Type.CRN, crn),
      needsPredicate(),
    )?.let {
      oasysApiRestClient.getScoredSectionsForAssessment(it, NeedsSection.entries)
    }

    if (sectionSummary == null) {
      throw EntityNotFoundException("No needs found for CRN: $crn")
    }

    return AssessmentNeedsDto.from(sectionSummary.assessmentNeeds(), sectionSummary.assessment.completedDate!!)
  }

  private fun SectionSummary.assessmentNeeds(): List<AssessmentNeedDto> = listOfNotNull(
    accommodation,
    educationTrainingEmployability,
    relationships,
    lifestyleAndAssociates,
    drugMisuse,
    alcoholMisuse,
    thinkingAndBehaviour,
    attitudes,
  ).map {
    AssessmentNeedDto(
      it.section.name,
      it.section.description,
      it.linkedToHarm.toBoolean(),
      it.linkedToReOffending.toBoolean(),
      it.severity,
    )
  }

  private fun ScoredAnswer.YesNo?.toBoolean(): Boolean? = when (this) {
    ScoredAnswer.YesNo.Yes -> true
    ScoredAnswer.YesNo.No -> false
    else -> null
  }

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }
}

fun needsPredicate(): (AssessmentSummary) -> Boolean = {
  it.assessmentType == AssessmentType.LAYER3.name && it.status == AssessmentStatus.COMPLETE.name && it.isWithin55Weeks()
}