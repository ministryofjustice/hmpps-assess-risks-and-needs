package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AllRoshRiskDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PersonIdentifier
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskRoshSummaryDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RoshRiskWidgetDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.config.RequestData
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.CommunityApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.OasysApiRestClient

@Service
class RiskService(
  private val oasysApiRestClient: OasysApiRestClient,
  private val communityClient: CommunityApiRestClient,
  private val auditService: AuditService,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun getRoshRisksByCrn(crn: String, timeframe: Long = 55): AllRoshRiskDto {
    log.info("Get Rosh Risk for crn $crn")
    auditService.sendEvent(EventType.ACCESSED_ROSH_RISKS, mapOf("crn" to crn))
    communityClient.verifyUserAccess(crn, RequestData.getUserName())

    return oasysApiRestClient.getRoshDetailForLatestCompletedAssessment(
      PersonIdentifier(PersonIdentifier.Type.CRN, crn),
      timeframe,
    ) ?: AllRoshRiskDto.empty
  }

  fun getRoshRisksWithoutLaoCheck(crn: String, timeframe: Long = 55): AllRoshRiskDto {
    auditService.sendEvent(EventType.ACCESSED_ROSH_RISKS, mapOf("crn" to crn))
    return oasysApiRestClient.getRoshDetailForLatestCompletedAssessment(
      PersonIdentifier(PersonIdentifier.Type.CRN, crn),
      timeframe,
    ) ?: AllRoshRiskDto.empty
  }

  fun getFulltextRoshRisksByCrn(crn: String, timeframe: Long = 55): AllRoshRiskDto {
    log.info("Get Full Text Rosh Risk for crn $crn")
    auditService.sendEvent(EventType.ACCESSED_ROSH_RISKS_FULLTEXT, mapOf("crn" to crn))
    communityClient.verifyUserAccess(crn, RequestData.getUserName())

    return oasysApiRestClient.getRoshDetailForLatestCompletedAssessment(
      PersonIdentifier(PersonIdentifier.Type.CRN, crn),
      timeframe,
    ) ?: AllRoshRiskDto.empty
  }

  fun getRoshRiskSummaryByCrn(crn: String, timeframe: Long = 55): RiskRoshSummaryDto {
    log.info("Get Rosh Risk summary for crn $crn")
    auditService.sendEvent(EventType.ACCESSED_ROSH_RISKS_SUMMARY, mapOf("crn" to crn))
    communityClient.verifyUserAccess(crn, RequestData.getUserName())

    return oasysApiRestClient.getRoshSummary(PersonIdentifier(PersonIdentifier.Type.CRN, crn), timeframe) ?: RiskRoshSummaryDto()
  }

  private fun calculateOverallRiskLevel(riskLevels: Map<RiskLevel?, List<String>>): RiskLevel? {
    val riskLevelsWithItems = riskLevels.filterValues { it.isNotEmpty() }.keys
    if (riskLevelsWithItems.contains(RiskLevel.VERY_HIGH)) return RiskLevel.VERY_HIGH
    if (riskLevelsWithItems.contains(RiskLevel.HIGH)) return RiskLevel.HIGH
    if (riskLevelsWithItems.contains(RiskLevel.MEDIUM)) return RiskLevel.MEDIUM
    if (riskLevelsWithItems.contains(RiskLevel.LOW)) return RiskLevel.LOW
    return null
  }

  fun getRoshRiskWidgetDataForCrn(crn: String, timeframe: Long = 55): RoshRiskWidgetDto {
    log.info("Get Rosh Risk widget data for crn $crn")
    return oasysApiRestClient.getRoshDetailForLatestCompletedAssessment(
      PersonIdentifier(PersonIdentifier.Type.CRN, crn),
      timeframe,
    ).toRoshRiskWidgetDto()
  }

  private fun AllRoshRiskDto?.toRoshRiskWidgetDto(): RoshRiskWidgetDto {
    val riskInCommunity = this?.summary?.riskInCommunity?.asWidgetRiskMap() ?: mapOf()
    val riskInCustody = this?.summary?.riskInCustody?.asWidgetRiskMap() ?: mapOf()

    return RoshRiskWidgetDto(
      assessedOn = this?.assessedOn,
      overallRisk = RoshRiskWidgetDto.riskLevelToString(
        calculateWidgetOverallRiskLevel(
          riskInCommunity,
          riskInCustody,
        ),
      ),
      riskInCommunity = RoshRiskWidgetDto.mapRiskLevelsToStrings(riskInCommunity),
      riskInCustody = RoshRiskWidgetDto.mapRiskLevelsToStrings(riskInCustody),
    )
  }

  private fun calculateWidgetOverallRiskLevel(
    riskInCommunity: Map<String, RiskLevel?>,
    riskInCustody: Map<String, RiskLevel?>,
  ): RiskLevel? {
    val riskInCommunityGroup = riskInCommunity.asGroups()
    val riskInCustodyGroup = riskInCustody.asGroups()

    return calculateOverallRiskLevel(riskInCommunityGroup + riskInCustodyGroup)
  }

  private fun Map<String, RiskLevel?>.asGroups(): Map<RiskLevel?, List<String>> = this.map { it.key to it.value }.groupBy({ it.second }, { it.first }).filterKeys { it != null }

  private fun Map<RiskLevel?, List<String>>.asWidgetRiskMap() = flatMap { entry ->
    entry.value.map { it to entry.key }
  }.toMap()
}
