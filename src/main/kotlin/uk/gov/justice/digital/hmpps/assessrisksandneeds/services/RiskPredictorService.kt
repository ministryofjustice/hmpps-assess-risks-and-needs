package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AllPredictorVersioned
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AllPredictorVersionedDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AllPredictorVersionedLegacyDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.IdentifierType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskScoresDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrPredictorDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrPredictorVersioned
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

  fun getAllRsrHistory(crn: String): List<RsrPredictorDto> {
    log.info("Retrieving RSR scores from each service")
    auditService.sendEvent(EventType.ACCESSED_RISK_PREDICTOR_HISTORY, mapOf("crn" to crn))
    communityClient.verifyUserAccess(crn, RequestData.getUserName())

    return getRsrScoresFromOasys(crn).sortedByDescending { it.completedDate }
  }

  private fun getRsrScoresFromOasys(crn: String): List<RsrPredictorDto> {
    val oasysPredictors = oasysClient.getRiskPredictorsForCompletedAssessments(crn)?.assessments ?: listOf()
    val oasysRsrPredictors = oasysPredictors.filter { it.hasRsrScores() }
    log.info("Retrieved ${oasysRsrPredictors.size} RSR scores from OASys")
    return RsrPredictorDto.from(oasysRsrPredictors)
  }

  fun getAllRsrScores(identifierType: IdentifierType, identifierValue: String): List<RsrPredictorVersioned<Any>> {
    log.info("Retrieving RSR scores from each service for ${identifierType.value}: $identifierValue")
    auditService.sendEvent(EventType.ACCESSED_RISK_PREDICTOR_HISTORY, mapOf(identifierType.value to identifierValue))
    communityClient.verifyUserAccess(identifierValue, RequestData.getUserName())
    val oasysPredictors = oasysClient.getRiskPredictorsForCompletedAssessments(identifierValue)?.assessments ?: listOf()
    val oasysRsrPredictors = oasysPredictors.filter { it.hasRsrScores() }
    log.info("Retrieved ${oasysRsrPredictors.size} RSR scores from OASys for ${identifierType.value}: $identifierValue")
    return RsrPredictorVersionedLegacyDto.from(oasysRsrPredictors).sortedByDescending { it.completedDate }
  }

  fun getAllRiskScores(crn: String): List<RiskScoresDto> {
    log.debug("Entered getAllRiskScores for crn: $crn")
    auditService.sendEvent(EventType.ACCESSED_RISK_PREDICTORS, mapOf("crn" to crn))
    communityClient.verifyUserAccess(crn, RequestData.getUserName())

    val oasysRiskPredictorsDto = oasysClient.getRiskPredictorsForCompletedAssessments(crn)
    return RiskScoresDto.from(oasysRiskPredictorsDto)
  }

  fun getAllRiskScores(identifierType: IdentifierType, identifierValue: String): List<AllPredictorVersioned<Any>> {
    log.debug("Entered getAllRiskScores for ${identifierType.value}: $identifierValue")
    auditService.sendEvent(EventType.ACCESSED_RISK_PREDICTORS, mapOf(identifierType.value to identifierValue))
    communityClient.verifyUserAccess(identifierValue, RequestData.getUserName())
    val oasysRiskPredictorsDto = oasysClient.getRiskPredictorsForCompletedAssessments(identifierValue)
    return oasysRiskPredictorsDto
      ?.assessments
      ?.filter { it.assessmentType in listOf("LAYER3", "LAYER1") }
      ?.map(AllPredictorVersionedLegacyDto::from)
      .orEmpty()
  }

  fun getAllRiskScoresByAssessmentId(id: Long): AllPredictorVersioned<Any> {
    log.debug("Entered getAllRiskScoresByAssessmentId for ID: $id")
    auditService.sendEvent(EventType.ACCESSED_RISK_PREDICTORS_BY_ASSESSMENT_ID, mapOf("id" to id))
    val oasysRiskPredictorsDto = oasysClient.getRiskPredictorsByAssessmentId(id)
    val crn = oasysRiskPredictorsDto?.probNumber
    communityClient.verifyUserAccess(crn!!, RequestData.getUserName())
    return oasysRiskPredictorsDto
      ?.assessments
      ?.first()
      ?.let { assessment: RisksCrAssPredictorAssessmentDto ->
        val version = assessment.rsrScoreDto.rsrAlgorithmVersion?.toIntOrNull() ?: 0
        if (version >= 6) {
          AllPredictorVersionedDto.from(assessment)
        } else {
          AllPredictorVersionedLegacyDto.from(assessment)
        }
      } ?: throw NoSuchElementException("Risk predictors for assessment $id not found")
  }

  fun getAllRiskScoresWithoutLaoCheck(crn: String): List<RiskScoresDto> {
    auditService.sendEvent(EventType.ACCESSED_RISK_PREDICTORS, mapOf("crn" to crn))
    val oasysRiskPredictorsDto = oasysClient.getRiskPredictorsForCompletedAssessments(crn)
    return RiskScoresDto.from(oasysRiskPredictorsDto)
  }
}
