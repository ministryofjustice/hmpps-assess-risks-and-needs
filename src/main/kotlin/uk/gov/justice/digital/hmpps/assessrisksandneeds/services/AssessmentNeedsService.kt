package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentNeedDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentNeedsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OffenderNeedDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OffenderNeedsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.AssessmentApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.EntityNotFoundException

@Service
class AssessmentNeedsService(private val assessmentClient: AssessmentApiRestClient) {
  fun getAssessmentNeeds(crn: String): AssessmentNeedsDto {
    log.info("Get needs for CRN: $crn")
    val offenderNeeds = assessmentClient.getNeedsForCompletedLastYearAssessment(crn)
    if (offenderNeeds?.historicStatus != "CURRENT") {
      throw EntityNotFoundException("Current needs for CRN: $crn could not be found")
    }
    if (offenderNeeds.needs.isNullOrEmpty()) {
      throw EntityNotFoundException("No needs found for CRN: $crn")
    }
    log.info("Retrieved ${offenderNeeds.needs.size} needs for CRN: $crn")
    val allOffenderNeedsMap = mapAllNeeds(offenderNeeds)
    val assessmentNeeds = toAssessmentNeedDtos(allOffenderNeedsMap)

    return AssessmentNeedsDto.from(assessmentNeeds, offenderNeeds)
  }

  private fun toAssessmentNeedDtos(needMapping: Map<String, OffenderNeedDto?>): List<AssessmentNeedDto> {
    return needMapping.map { (section, offenderNeed) ->
      if (offenderNeed == null) {
        AssessmentNeedDto.from(section, needNames)
      } else {
        AssessmentNeedDto.from(section, offenderNeed)
      }
    }
  }

  private fun mapAllNeeds(offenderNeeds: OffenderNeedsDto): Map<String, OffenderNeedDto?> {
    return needNames.keys.associateWith { needName ->
      offenderNeeds.needs.firstOrNull { need ->
        need.section.equals(needName)
      }
    }
  }

  val needNames = mapOf(
    "ACCOMMODATION" to "Accommodation",
    "EDUCATION_TRAINING_AND_EMPLOYABILITY" to "Education training and employability",
    "FINANCIAL_MANAGEMENT_AND_INCOME" to "Financial management and income",
    "RELATIONSHIPS" to "Relationships",
    "LIFESTYLE_AND_ASSOCIATES" to "Lifestyle and associates",
    "DRUG_MISUSE" to "Drug misuse",
    "ALCOHOL_MISUSE" to "Alcohol misuse",
    "EMOTIONAL_WELL_BEING" to "Emotional well being",
    "THINKING_AND_BEHAVIOUR" to "Thinking and behaviour",
    "ATTITUDES" to "Attitudes"
  )

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }
}
