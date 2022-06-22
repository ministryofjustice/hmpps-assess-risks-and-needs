package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentOffenceDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.AssessmentApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.EntityNotFoundException

@Service
class AssessmentOffenceService(private val assessmentClient: AssessmentApiRestClient) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }
  fun getAssessmentOffence(crn: String): AssessmentOffenceDto {
    log.info("Get assessment offence for CRN: $crn")

    val assessmentOffenceDto = assessmentClient.getAssessmentOffence(
      crn = crn,
      limitedAccessOffender = "LIMIT"
    ) ?: throw EntityNotFoundException("Assessment offence not found for CRN: $crn")
    mapTimelineToAssessments(assessmentOffenceDto)

    return assessmentOffenceDto
  }

  private fun mapTimelineToAssessments(assessmentOffenceDto: AssessmentOffenceDto) {

    val filteredTimeLine = assessmentOffenceDto.timeline.filter { it.status != "COMPLETE" }
    val assessmentDtos = filteredTimeLine.map {
      AssessmentDto(
        dateCompleted = it.completedDate,
        assessmentStatus = it.status,
        initiationDate = it.initiationDate
      )
    }
    assessmentOffenceDto.assessments = (assessmentOffenceDto.assessments + assessmentDtos).sortedBy { it.initiationDate }
    assessmentOffenceDto.timeline = emptyList()
  }
}
