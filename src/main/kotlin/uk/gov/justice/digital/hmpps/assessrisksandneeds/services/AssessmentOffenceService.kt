package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentOffenceDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.BasicAssessmentSummary
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PersonIdentifier
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.SanIndicatorResponse
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.SexualOffenceDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.Timeline
import uk.gov.justice.digital.hmpps.assessrisksandneeds.config.Clock
import uk.gov.justice.digital.hmpps.assessrisksandneeds.config.RequestData
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.CommunityApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.OasysApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.MappsAssessmentDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.MappsAssessmentTimeline
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysAssessmentOffenceDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.TimelineDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.EntityNotFoundException

@Service
class AssessmentOffenceService(
  private val oasysApiRestClient: OasysApiRestClient,
  private val communityClient: CommunityApiRestClient,
  private val auditService: AuditService,
  private val clock: Clock,
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

  fun getAssessmentTimeline(identifier: PersonIdentifier): Timeline {
    log.info("Getting assessment timeline for $identifier")
    return oasysApiRestClient.getAssessmentTimeline(identifier)?.let { t ->
      // copy and filter out STANDALONE type (OASYS internal implementation for RSR) as not a real type
      Timeline(
        t.timeline.filter {
          it.assessmentType in listOf(AssessmentType.LAYER3.name, AssessmentType.LAYER1.name)
        },
      )
    } ?: throw EntityNotFoundException("Assessment timeline not found for $identifier")
  }

  fun getSanIndicator(crn: String, timeframe: Long = 55): SanIndicatorResponse {
    oasysApiRestClient.getLatestAssessment(
      PersonIdentifier(PersonIdentifier.Type.CRN, crn),
      needsPredicate(timeframe = timeframe, clock = clock),
    )?.let {
      oasysApiRestClient.getAssessmentSummaryIndicators(it, crn)?.assessments?.firstOrNull()?.let { indicator ->
        return SanIndicatorResponse(crn, indicator.getSanIndicator())
      } ?: throw EntityNotFoundException("No assessment summary found for CRN: $crn")
    } ?: throw EntityNotFoundException("No assessment found for CRN: $crn")
  }

  fun getSexuallyMotivatedOffenceDetails(crn: String): SexualOffenceDto = oasysApiRestClient.getLatestAssessment(PersonIdentifier(PersonIdentifier.Type.CRN, crn)) {
    // the response is valid from any assessment, including WIP assessments, regardless of layer1 or layer 3
    // note: we still need to filter out STANDALONE assessment types
    it.assessmentType in listOf(AssessmentType.LAYER3.name, AssessmentType.LAYER1.name)
  }?.let {
    oasysApiRestClient.getOffenderInformationAndPredictorsSection(it).assessments.firstOrNull()
  }?.let {
    SexualOffenceDto(it.everCommittedSexualOffence)
  } ?: throw EntityNotFoundException("No assessment found for CRN: $crn")

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

  /**
   * Gets the latest COMPLETE assessment data for MAPPS integration.
   * Fetches assessor/countersigner names from section1 endpoint for all assessments.
   */
  fun getLatestCompleteAssessmentsForMapps(identifier: PersonIdentifier): MappsAssessmentTimeline {
    log.info("Getting latest complete assessment for MAPPS: $identifier")

    // 1. Get timeline (list of all assessments)
    val timeline = oasysApiRestClient.getAssessmentTimeline(identifier)
      ?: throw EntityNotFoundException("Assessment timeline not found for $identifier")

    // 2. Filter to COMPLETE assessments only (LAYER1 and LAYER3)
    val completeAssessments = timeline.timeline
      .filter {
        it.assessmentType in listOf(AssessmentType.LAYER3.name, AssessmentType.LAYER1.name) &&
          it.status == "COMPLETE"
      }
      .sortedWith(compareByDescending<BasicAssessmentSummary> { it.completedDate ?: it.initiationDate })

    if (completeAssessments.isEmpty()) {
      throw EntityNotFoundException("No complete assessment found for $identifier")
    }

    // 3. For each COMPLETE assessment, fetch section1 to get assessor/countersigner details
    val mappasAssessments = completeAssessments.mapNotNull { assessment ->
      try {
        // Fetch section1 data which contains assessor/countersigner names
        val section1Data = oasysApiRestClient.getOffenderInformationAndPredictorsSection(assessment)
        val assessorInfo = section1Data.assessments.firstOrNull()

        MappsAssessmentDto(
          assessmentId = assessment.assessmentId,
          initiationDate = assessment.initiationDate,
          dateCompleted = assessment.completedDate,
          assessmentType = assessment.assessmentType,
          assessmentStatus = assessment.status,
          assessorName = assessorInfo?.assessorName,
          countersignerName = assessorInfo?.countersignerName,  // May be null - that's OK
        )
      } catch (e: Exception) {
        log.warn("Failed to fetch section1 data for assessment ${assessment.assessmentId}: ${e.message}. Skipping this assessment.")
        null  // Skip this assessment if section1 fetch fails
      }
    }

    if (mappasAssessments.isEmpty()) {
      throw EntityNotFoundException("No assessments with valid section1 data found for $identifier")
    }

    log.info("Retrieved ${mappasAssessments.size} complete MAPPS assessments for $identifier")
    return MappsAssessmentTimeline(assessments = mappasAssessments)
  }

  private fun mapAssessmentDtos(oasysAssessmentOffenceDto: OasysAssessmentOffenceDto) = oasysAssessmentOffenceDto.assessments.map {
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

  private fun mapSummaryAssessmentDtos(filteredTimeLine: List<TimelineDto>) = filteredTimeLine.map {
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
