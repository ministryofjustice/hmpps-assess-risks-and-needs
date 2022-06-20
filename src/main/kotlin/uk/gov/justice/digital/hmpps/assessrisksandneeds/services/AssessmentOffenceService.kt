package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
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

    return assessmentClient.getAssessmentOffence(
      crn = crn,
      limitedAccessOffender = "LIMIT",
      assessmentStatus = "COMPLETE",
      nthLatestAssessment = 0
    ) ?: throw EntityNotFoundException("Assessment offence not found for CRN: $crn")
  }
}
