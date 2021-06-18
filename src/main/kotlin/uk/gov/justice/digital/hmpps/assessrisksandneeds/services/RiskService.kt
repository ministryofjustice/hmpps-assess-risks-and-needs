package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AllRoshRiskDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OtherRoshRisksDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ResponseDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskRoshSummaryDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RoshRiskToSelfDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.AssessmentApiRestClient

@Service
class RiskService(private val assessmentClient: AssessmentApiRestClient) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun getRoshRisksByCrn(crn: String): AllRoshRiskDto {
    log.info("Get Rosh Risk for crn $crn")
    val sectionsAnswers = assessmentClient.getRoshSectionsForCompletedLastYearAssessment(crn)
    log.info("Section answers for crn $crn $sectionsAnswers")
    return AllRoshRiskDto(
      RoshRiskToSelfDto(
        RiskDto(ResponseDto.DK, "Previous concerns", "Current concerns"),
        RiskDto(ResponseDto.NO, "Previous concerns", "Current concerns"),
        RiskDto(ResponseDto.YES, "Previous concerns", "Current concerns"),
        RiskDto(ResponseDto.DK, "Previous concerns", "Current concerns"),
        RiskDto(null, null, null),
      ),
      OtherRoshRisksDto(
        RiskDto(ResponseDto.YES, "Previous concerns", "Current concerns"),
        RiskDto(ResponseDto.DK, "Previous concerns", "Current concerns"),
        RiskDto(null, null, null)
      ),
      RiskRoshSummaryDto(
        "whoisAtRisk",
        "natureOfRisk",
        "riskImminence",
        "riskIncreaseFactors",
        "riskMitigationFactors",
        mapOf(RiskLevel.HIGH to listOf("children")),
        mapOf(RiskLevel.MEDIUM to listOf("known adult"))
      )
    )
  }
}
