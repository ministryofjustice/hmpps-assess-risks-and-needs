package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PredictorType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskScoresDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrPredictorDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.config.RequestData
import uk.gov.justice.digital.hmpps.assessrisksandneeds.jpa.respositories.OffenderPredictorsHistoryRepository
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.CommunityApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.OasysApiRestClient

@Service
class RiskPredictorService(
  private val oasysClient: OasysApiRestClient,
  private val communityClient: CommunityApiRestClient,
  private val offenderPredictorsHistoryRepository: OffenderPredictorsHistoryRepository,
  private val auditService: AuditService,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun getAllRsrHistory(crn: String): List<RsrPredictorDto> {
    log.info("Retrieving RSR scores from each service")
    auditService.sendEvent(EventType.ACCESSED_RISK_PREDICTOR_HISTORY, mapOf("crn" to crn))
    communityClient.verifyUserAccess(crn, RequestData.getUserName())

    val oasysRsrPredictors = getRsrScoresFromOasys(crn)
    val arnRsrPredictors = getRsrScoresFromArn(crn)
    return oasysRsrPredictors.plus(arnRsrPredictors).sortedByDescending { it.completedDate }
  }

  private fun getRsrScoresFromOasys(crn: String): List<RsrPredictorDto> {
    val oasysPredictors = oasysClient.getRiskPredictorsForCompletedAssessments(crn)?.assessments ?: listOf()
    val oasysRsrPredictors = oasysPredictors.filter { it.hasRsrScores() }
    log.info("Retrieved ${oasysRsrPredictors.size} RSR scores from OASys")
    return RsrPredictorDto.from(oasysRsrPredictors)
  }

  private fun getRsrScoresFromArn(crn: String): List<RsrPredictorDto> {
    val arnPredictors = offenderPredictorsHistoryRepository.findAllByCrn(crn)
    val arnRsrPredictors = arnPredictors.filter { it.predictorType == PredictorType.RSR }
    log.info("Retrieved ${arnRsrPredictors.size} RSR scores from ARN")
    return RsrPredictorDto.from(arnRsrPredictors)
  }

  fun getAllRiskScores(crn: String): List<RiskScoresDto> {
    log.debug("Entered getAllRiskScores for crn: $crn")
    auditService.sendEvent(EventType.ACCESSED_RISK_PREDICTORS, mapOf("crn" to crn))
    communityClient.verifyUserAccess(crn, RequestData.getUserName())

    val oasysRiskPredictorsDto = oasysClient.getRiskPredictorsForCompletedAssessments(crn)
    return RiskScoresDto.from(oasysRiskPredictorsDto)
  }
}
