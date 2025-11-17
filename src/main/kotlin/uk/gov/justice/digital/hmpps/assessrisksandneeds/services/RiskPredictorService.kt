package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.IdentifierType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskScoresDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrPredictorDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrPredictorVersioned
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrPredictorVersionedDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrPredictorVersionedLegacyDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.config.RequestData
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.CommunityApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.OasysApiRestClient

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

  fun getAllRsrScores(identifierType: String, identifierValue: String): List<RsrPredictorVersioned<Any>> {
    val validIdentifierType = IdentifierType.fromString(identifierType)
    log.info("Retrieving RSR scores from each service for $validIdentifierType: $identifierValue")
    auditService.sendEvent(EventType.ACCESSED_RISK_PREDICTOR_HISTORY, mapOf(validIdentifierType to identifierValue))
    communityClient.verifyUserAccess(identifierValue, RequestData.getUserName())
    val oasysPredictors = oasysClient.getRiskPredictorsForCompletedAssessments(identifierValue)?.assessments ?: listOf()
    val oasysRsrPredictors = oasysPredictors.filter { it.hasRsrScores() }
    log.info("Retrieved ${oasysRsrPredictors.size} RSR scores from OASys for $validIdentifierType: $identifierValue")
    return RsrPredictorVersionedLegacyDto.from(oasysRsrPredictors)
  }

  fun getAllRiskScores(crn: String): List<RiskScoresDto> {
    log.debug("Entered getAllRiskScores for crn: $crn")
    auditService.sendEvent(EventType.ACCESSED_RISK_PREDICTORS, mapOf("crn" to crn))
    communityClient.verifyUserAccess(crn, RequestData.getUserName())

    val oasysRiskPredictorsDto = oasysClient.getRiskPredictorsForCompletedAssessments(crn)
    return RiskScoresDto.from(oasysRiskPredictorsDto)
  }

  fun getAllRiskScoresWithoutLaoCheck(crn: String): List<RiskScoresDto> {
    auditService.sendEvent(EventType.ACCESSED_RISK_PREDICTORS, mapOf("crn" to crn))
    val oasysRiskPredictorsDto = oasysClient.getRiskPredictorsForCompletedAssessments(crn)
    return RiskScoresDto.from(oasysRiskPredictorsDto)
  }
}
