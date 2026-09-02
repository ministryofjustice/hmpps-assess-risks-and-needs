package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AllPredictorVersioned
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AllPredictorVersionedDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AllPredictorVersionedLegacyDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.IdentifierType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrPredictorVersioned
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrPredictorVersionedDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrPredictorVersionedLegacyDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.config.RequestData
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.CommunityApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.OasysApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.RisksCrAssPredictorAssessmentDto

@Service
class RiskPredictorService(
  private val oasysClient: OasysApiRestClient,
  private val communityClient: CommunityApiRestClient,
  private val auditService: AuditService,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun getAllRsrScores(identifierType: IdentifierType, identifierValue: String): List<RsrPredictorVersioned<Any>> {
    log.info("Retrieving RSR scores from each service for ${identifierType.value}: $identifierValue")
    auditService.sendEvent(EventType.ACCESSED_RISK_PREDICTOR_HISTORY, mapOf(identifierType.value to identifierValue))
    communityClient.verifyUserAccess(identifierValue, RequestData.getUserName())
    val oasysPredictors = oasysClient.getRiskPredictorsForCompletedAssessments(identifierValue)?.assessments ?: listOf()
    val oasysRsrPredictors = oasysPredictors.filter { it.hasRsrScores() }
    log.info("Retrieved ${oasysRsrPredictors.size} RSR scores from OASys for ${identifierType.value}: $identifierValue")
    return oasysRsrPredictors.map { assessment ->
      val version = assessment.rsrScoreDto.rsrAlgorithmVersion?.toIntOrNull()
      // If version is null, it's a legacy assessment
      if (version != null && version >= 6) {
        RsrPredictorVersionedDto.from(assessment)
      } else {
        RsrPredictorVersionedLegacyDto.from(assessment)
      }
    }.sortedByDescending { it.completedDate }
  }

  fun getAllRiskScores(
    identifierType: IdentifierType,
    identifierValue: String,
    includeStandaloneAssessments: Boolean,
  ): List<AllPredictorVersioned<Any>> {
    log.debug("Entered getAllRiskScores for ${identifierType.value}: $identifierValue")
    communityClient.verifyUserAccess(identifierValue, RequestData.getUserName())
    return getAllRiskScoresWithoutLaoCheck(identifierType, identifierValue, includeStandaloneAssessments)
  }

  fun getAllRiskScoresWithoutLaoCheck(
    identifierType: IdentifierType,
    identifierValue: String,
    includeStandaloneAssessments: Boolean,
  ): List<AllPredictorVersioned<Any>> {
    log.debug("Entered getAllRiskScoresWithoutLaoCheck for ${identifierType.value}: $identifierValue")
    auditService.sendEvent(EventType.ACCESSED_RISK_PREDICTORS, mapOf(identifierType.value to identifierValue))
    val oasysRiskPredictorsDto = oasysClient.getRiskPredictorsForCompletedAssessments(identifierValue)
    return oasysRiskPredictorsDto
      ?.assessments
      ?.filter {
        it.assessmentType in listOfNotNull(
          "LAYER3",
          "LAYER1",
          "STANDALONE".takeIf { includeStandaloneAssessments },
        )
      }
      ?.map { assessment ->
        val version = assessment.rsrScoreDto.rsrAlgorithmVersion?.toIntOrNull()
        // If version is null, it's a legacy assessment
        if (version != null && version >= 6) {
          AllPredictorVersionedDto.from(assessment)
        } else {
          AllPredictorVersionedLegacyDto.from(assessment)
        }
      }
      .orEmpty()
  }

  fun getAllRiskScoresByAssessmentId(id: Long): AllPredictorVersioned<Any> {
    log.debug("Entered getAllRiskScoresByAssessmentId for ID: $id")
    auditService.sendEvent(EventType.ACCESSED_RISK_PREDICTORS_BY_ASSESSMENT_ID, mapOf("id" to id))
    val oasysRiskPredictorsDto = oasysClient.getRiskPredictorsByAssessmentId(id)
    return oasysRiskPredictorsDto
      ?.assessments
      ?.first()
      ?.let { assessment: RisksCrAssPredictorAssessmentDto ->
        val version = assessment.rsrScoreDto.rsrAlgorithmVersion?.toIntOrNull()
        // If version is null, it's a legacy assessment
        if (version != null && version >= 6) {
          AllPredictorVersionedDto.from(assessment)
        } else {
          AllPredictorVersionedLegacyDto.from(assessment)
        }
      } ?: throw NoSuchElementException("Risk predictors for assessment with id: $id not found")
  }
}
