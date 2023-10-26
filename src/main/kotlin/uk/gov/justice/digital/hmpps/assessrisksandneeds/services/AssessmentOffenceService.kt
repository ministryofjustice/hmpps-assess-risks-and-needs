package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentOffenceDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.config.RequestData
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.CommunityApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.OasysApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysAssessmentOffenceDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.TimelineDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.EntityNotFoundException

@Service
class AssessmentOffenceService(
  private val oasysApiRestClient: OasysApiRestClient,
  private val communityClient: CommunityApiRestClient,
  private val auditService: AuditService,
) {

  private val limitedAccess = "ALLOW"

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun getAssessmentOffence(crn: String): AssessmentOffenceDto {
    log.info("Get assessment offence for CRN: $crn")
    auditService.sendEvent(EventType.ACCESSED_OFFENCE_DETAILS, mapOf("crn" to crn))
    communityClient.verifyUserAccess(crn, RequestData.getUserName())

    val assessmentOffenceDto = oasysApiRestClient.getAssessmentOffence(
      crn = crn,
      limitedAccessOffender = limitedAccess,
    ) ?: throw EntityNotFoundException("Assessment offence not found for CRN: $crn")

    return mapTimelineToAssessments(assessmentOffenceDto)
  }

  private fun mapTimelineToAssessments(oasysAssessmentOffenceDto: OasysAssessmentOffenceDto): AssessmentOffenceDto {
    val assessmentIds = oasysAssessmentOffenceDto.assessments.map { it.assessmentPk }
    val filteredTimeLine = oasysAssessmentOffenceDto.timeline.filter { it.assessmentPk !in assessmentIds }
    val summaryAssessmentDtos = mapSummaryAssessmentDtos(filteredTimeLine)

    val assessmentDtos = mapAssessmentDtos(oasysAssessmentOffenceDto)

    return AssessmentOffenceDto(
      crn = oasysAssessmentOffenceDto.crn,
      limitedAccessOffender = oasysAssessmentOffenceDto.limitedAccessOffender,
      assessments = (summaryAssessmentDtos + assessmentDtos).sortedBy { it.initiationDate },
    )
  }

  private fun mapAssessmentDtos(oasysAssessmentOffenceDto: OasysAssessmentOffenceDto) =
    oasysAssessmentOffenceDto.assessments.map {
      AssessmentDto(
        assessmentId = it.assessmentPk,
        assessmentType = it.assessmentType,
        assessmentStatus = it.assessmentStatus,
        dateCompleted = it.dateCompleted,
        initiationDate = it.initiationDate,
        assessorSignedDate = it.assessorSignedDate,
        superStatus = it.superStatus,
        offence = it.offence,
        disinhibitors = it.disinhibitors,
        patternOfOffending = it.patternOfOffending,
        offenceInvolved = it.offenceInvolved,
        specificWeapon = it.specificWeapon,
        victimPerpetratorRelationship = it.victimPerpetratorRelationship,
        victimOtherInfo = it.victimOtherInfo,
        evidencedMotivations = it.evidencedMotivations,
        offenceDetails = it.offenceDetails,
        victimDetails = it.victimDetails,
        laterWIPAssessmentExists = it.laterWIPAssessmentExists,
        latestWIPDate = it.latestWIPDate,
        laterSignLockAssessmentExists = it.laterSignLockAssessmentExists,
        latestSignLockDate = it.latestSignLockDate,
        laterPartCompUnsignedAssessmentExists = it.laterPartCompUnsignedAssessmentExists,
        latestPartCompUnsignedDate = it.latestPartCompUnsignedDate,
        laterPartCompSignedAssessmentExists = it.laterPartCompSignedAssessmentExists,
        latestPartCompSignedDate = it.latestPartCompSignedDate,
        laterCompleteAssessmentExists = it.laterCompleteAssessmentExists,
        latestCompleteDate = it.latestCompleteDate,
      )
    }

  private fun mapSummaryAssessmentDtos(filteredTimeLine: List<TimelineDto>) =
    filteredTimeLine.map {
      AssessmentDto(
        assessmentId = it.assessmentPk,
        assessmentType = it.assessmentType,
        dateCompleted = it.completedDate,
        assessmentStatus = it.status,
        initiationDate = it.initiationDate,
        partcompStatus = it.partcompStatus,
      )
    }
}
