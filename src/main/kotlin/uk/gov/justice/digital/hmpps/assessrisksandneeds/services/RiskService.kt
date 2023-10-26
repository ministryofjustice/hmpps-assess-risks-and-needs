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
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RoshRiskWidgetDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.OffenderAssessmentApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.QuestionAnswerDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.SectionAnswersDto

@Service
class RiskService(
  private val assessmentClient: OffenderAssessmentApiRestClient,
  private val auditService: AuditService,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun getRoshRisksByCrn(crn: String): AllRoshRiskDto {
    log.info("Get Rosh Risk for crn $crn")
    auditService.sendEvent(EventType.ACCESSED_ROSH_RISKS, mapOf("crn" to crn))

    val sectionsAnswers = assessmentClient.getRoshSectionsForCompletedLastYearAssessment(crn)
    log.info("Section answers for crn $crn number of sections : ${sectionsAnswers?.sections?.size}")
    return AllRoshRiskDto(
      riskToSelf = sectionsAnswers.toRoshRiskToSelfDto(),
      otherRisks = sectionsAnswers.toOtherRoshRisksDto(),
      summary = sectionsAnswers.toRiskRoshSummaryDto(),
      assessedOn = sectionsAnswers?.assessedOn,
    )
  }
  fun getFulltextRoshRisksByCrn(crn: String): AllRoshRiskDto {
    log.info("Get Full Text Rosh Risk for crn $crn")
    auditService.sendEvent(EventType.ACCESSED_ROSH_RISKS_FULLTEXT, mapOf("crn" to crn))

    val sectionsAnswers = assessmentClient.getRoshSectionsForCompletedLastYearAssessment(crn)
    log.info("Section answers for crn $crn number of sections : ${sectionsAnswers?.sections?.size}")
    return AllRoshRiskDto(
      riskToSelf = sectionsAnswers.toRoshRiskToSelfDto(fullText = true),
      otherRisks = sectionsAnswers.toOtherRoshRisksDto(),
      summary = sectionsAnswers.toRiskRoshSummaryDto(),
      assessedOn = sectionsAnswers?.assessedOn,
    )
  }

  fun getRoshRiskSummaryByCrn(crn: String): RiskRoshSummaryDto {
    log.info("Get Rosh Risk summary for crn $crn")
    auditService.sendEvent(EventType.ACCESSED_ROSH_RISKS_SUMMARY, mapOf("crn" to crn))

    val sectionsAnswers = assessmentClient.getRoshSectionsForCompletedLastYearAssessment(crn)
    log.info("Section answers for crn $crn number of sections : ${sectionsAnswers?.sections?.size}")
    return sectionsAnswers.toRiskRoshSummaryDto()
  }

  private fun SectionAnswersDto?.toRiskRoshSummaryDto(): RiskRoshSummaryDto {
    val roshSumAnswers = this?.sections?.get(SectionHeader.ROSH_SUMMARY.value)

    val riskInCommunity = roshSumAnswers.toRiskInCommunityMap().asGroups()
    val riskInCustody = roshSumAnswers.toRiskInCustodyMap().asGroups()

    return RiskRoshSummaryDto(
      findAnswer(RoshQuestionCodes.WHO_IS_AT_RISK, roshSumAnswers)?.freeFormText,
      findAnswer(RoshQuestionCodes.NATURE_OF_RISK, roshSumAnswers)?.freeFormText,
      findAnswer(RoshQuestionCodes.RISK_IMMINENCE, roshSumAnswers)?.freeFormText,
      findAnswer(RoshQuestionCodes.RISK_INCREASE_FACTORS, roshSumAnswers)?.freeFormText,
      findAnswer(RoshQuestionCodes.RISK_MITIGATION_FACTORS, roshSumAnswers)?.freeFormText,
      riskInCommunity,
      riskInCustody,
      this?.assessedOn,
      calculateOverallRiskLevel(riskInCustody + riskInCommunity),
    )
  }

  private fun calculateOverallRiskLevel(riskLevels: Map<RiskLevel?, List<String>>): RiskLevel? {
    val riskLevelsWithItems = riskLevels.filterValues { it.isNotEmpty() }.keys
    if (riskLevelsWithItems.contains(RiskLevel.VERY_HIGH)) return RiskLevel.VERY_HIGH
    if (riskLevelsWithItems.contains(RiskLevel.HIGH)) return RiskLevel.HIGH
    if (riskLevelsWithItems.contains(RiskLevel.MEDIUM)) return RiskLevel.MEDIUM
    if (riskLevelsWithItems.contains(RiskLevel.LOW)) return RiskLevel.LOW
    return null
  }

  private fun SectionAnswersDto?.toOtherRoshRisksDto(): OtherRoshRisksDto {
    val roshAnswers = this?.sections?.get(SectionHeader.ROSH_SCREENING.value)
    return OtherRoshRisksDto(
      ResponseDto.fromString(findAnswer(RoshQuestionCodes.ESCAPE_OR_ABSCOND, roshAnswers)?.staticText),
      ResponseDto.fromString(
        findAnswer(
          RoshQuestionCodes.CONTROL_ISSUES_DISRUPTIVE_BEHAVIOUR,
          roshAnswers,
        )?.staticText,
      ),
      ResponseDto.fromString(findAnswer(RoshQuestionCodes.BREACH_OF_TRUST, roshAnswers)?.staticText),
      ResponseDto.fromString(findAnswer(RoshQuestionCodes.RISK_TO_OTHER_PRISONERS, roshAnswers)?.staticText),
      this?.assessedOn,
    )
  }

  private fun SectionAnswersDto?.toRoshRiskToSelfDto(fullText: Boolean = false): RoshRiskToSelfDto {
    val roshFullAnswers = this?.sections?.get(SectionHeader.ROSH_FULL_ANALYSIS.value)
    val roshAnswers = this?.sections?.get(SectionHeader.ROSH_SCREENING.value)
    val selfHarmCurrentRisk = roshFullAnswers.toResponseForQuestion(RoshQuestionCodes.SELF_HARM_CURRENT_RISK)
    val suicideCurrentRisk = roshFullAnswers.toResponseForQuestion(RoshQuestionCodes.SUICIDE_CURRENT_RISK)
    val custodyCurrentRisk = roshFullAnswers.toResponseForQuestion(RoshQuestionCodes.CUSTODY_CURRENT_RISK)
    val hostelSettingCurrentRisk =
      roshFullAnswers.toResponseForQuestion(RoshQuestionCodes.HOSTEL_SETTING_CURRENT_RISK)
    val vulnerabilityCurrentRisk = roshFullAnswers.toResponseForQuestion(RoshQuestionCodes.VULNERABILITY_CURRENT_RISK)
    val suicidePreviousRisk = roshFullAnswers.toResponseForQuestion(RoshQuestionCodes.SUICIDE_PREVIOUS_RISK)

    val selfHarmPreviousRisk = roshFullAnswers.toResponseForQuestion(RoshQuestionCodes.SELF_HARM_PREVIOUS_RISK)
    val custodyPreviousRisk = roshFullAnswers.toResponseForQuestion(RoshQuestionCodes.CUSTODY_PREVIOUS_RISK)
    val hostelSettingPreviousRisk =
      roshFullAnswers.toResponseForQuestion(RoshQuestionCodes.HOSTEL_SETTING_PREVIOUS_RISK)
    val vulnerabilityPreviousRisk = roshFullAnswers.toResponseForQuestion(RoshQuestionCodes.VULNERABILITY_PREVIOUS_RISK)

    return RoshRiskToSelfDto(
      suicide = RiskDto(
        risk = roshAnswers.toResponseForQuestion(RoshQuestionCodes.SUICIDE_RISK),
        previous = suicidePreviousRisk,
        previousConcernsText = findConcernsFreeText(
          suicidePreviousRisk,
          roshFullAnswers,
          RoshQuestionCodes.SUICIDE_SELF_HARM_PREVIOUS_RISK_TEXT,
          fullText,
        ),
        current = suicideCurrentRisk,
        currentConcernsText = findConcernsFreeText(
          suicideCurrentRisk,
          roshFullAnswers,
          RoshQuestionCodes.SUICIDE_SELF_HARM_CURRENT_RISK_TEXT,
          fullText,
        ),
      ),
      selfHarm = RiskDto(
        risk = roshAnswers.toResponseForQuestion(RoshQuestionCodes.SELF_HARM_RISK),
        previous = selfHarmPreviousRisk,
        previousConcernsText = findConcernsFreeText(
          selfHarmPreviousRisk,
          roshFullAnswers,
          RoshQuestionCodes.SUICIDE_SELF_HARM_PREVIOUS_RISK_TEXT,
          fullText,
        ),
        current = selfHarmCurrentRisk,
        currentConcernsText = findConcernsFreeText(
          selfHarmCurrentRisk,
          roshFullAnswers,
          RoshQuestionCodes.SUICIDE_SELF_HARM_CURRENT_RISK_TEXT,
          fullText,
        ),
      ),
      custody = RiskDto(
        risk = roshAnswers.toResponseForQuestion(RoshQuestionCodes.CUSTODY_RISK),
        previous = custodyPreviousRisk,
        previousConcernsText = findConcernsFreeText(
          custodyPreviousRisk,
          roshFullAnswers,
          RoshQuestionCodes.CUSTODY_HOSTEL_SETTING_PREVIOUS_RISK_TEXT,
          fullText,
        ),
        current = custodyCurrentRisk,
        currentConcernsText = findConcernsFreeText(
          custodyCurrentRisk,
          roshFullAnswers,
          RoshQuestionCodes.CUSTODY_HOSTEL_SETTING_CURRENT_RISK_TEXT,
          fullText,
        ),
      ),
      hostelSetting = RiskDto(
        risk = roshAnswers.toResponseForQuestion(RoshQuestionCodes.CUSTODY_RISK),
        previous = hostelSettingPreviousRisk,
        previousConcernsText = findConcernsFreeText(
          hostelSettingPreviousRisk,
          roshFullAnswers,
          RoshQuestionCodes.CUSTODY_HOSTEL_SETTING_PREVIOUS_RISK_TEXT,
          fullText,
        ),
        current = hostelSettingCurrentRisk,
        currentConcernsText = findConcernsFreeText(
          hostelSettingCurrentRisk,
          roshFullAnswers,
          RoshQuestionCodes.CUSTODY_HOSTEL_SETTING_CURRENT_RISK_TEXT,
          fullText,
        ),
      ),
      vulnerability = RiskDto(
        risk = roshAnswers.toResponseForQuestion(RoshQuestionCodes.VULNERABILITY_RISK),
        previous = vulnerabilityPreviousRisk,
        previousConcernsText = findConcernsFreeText(
          vulnerabilityPreviousRisk,
          roshFullAnswers,
          RoshQuestionCodes.VULNERABILITY_PREVIOUS_RISK_TEXT,
          fullText,
        ),
        current = vulnerabilityCurrentRisk,
        currentConcernsText = findConcernsFreeText(
          vulnerabilityCurrentRisk,
          roshFullAnswers,
          RoshQuestionCodes.VULNERABILITY_CURRENT_RISK_TEXT,
          fullText,
        ),
      ),
      this?.assessedOn,
    )
  }

  private fun Collection<QuestionAnswerDto>?.toResponseForQuestion(questionCode: RoshQuestionCodes) =
    ResponseDto.fromString(
      findAnswer(
        questionCode,
        this,
      )?.staticText,
    )

  private fun findConcernsFreeText(
    riskResponse: ResponseDto?,
    answers: Collection<QuestionAnswerDto>?,
    questionCode: RoshQuestionCodes,
    fullText: Boolean,
  ) = if (fullText || riskResponse == ResponseDto.YES) {
    findAnswer(questionCode, answers)?.freeFormText
  } else {
    null
  }

  private fun findAnswer(questionCode: RoshQuestionCodes, answers: Collection<QuestionAnswerDto>?): QuestionAnswerDto? {
    return answers?.find { q -> q.refQuestionCode.equals(questionCode.value) }
  }

  fun getRoshRiskWidgetDataForCrn(crn: String): RoshRiskWidgetDto {
    log.info("Get Rosh Risk widget data for crn $crn")
    val sectionsAnswers = assessmentClient.getRoshSectionsForCompletedLastYearAssessment(crn)
    log.info("Section answers for crn $crn number of sections : ${sectionsAnswers?.sections?.size}")
    return sectionsAnswers.toRoshRiskWidgetDto()
  }

  private fun SectionAnswersDto?.toRoshRiskWidgetDto(): RoshRiskWidgetDto {
    val roshSumAnswers = this?.sections?.get(SectionHeader.ROSH_SUMMARY.value)

    val riskInCommunity = roshSumAnswers.toRiskInCommunityMap()
    val riskInCustody = roshSumAnswers.toRiskInCustodyMap()

    return RoshRiskWidgetDto(
      assessedOn = this?.assessedOn,
      overallRisk = RoshRiskWidgetDto.riskLevelToString(calculateWidgetOverallRiskLevel(riskInCommunity, riskInCustody)),
      riskInCommunity = RoshRiskWidgetDto.mapRiskLevelsToStrings(riskInCommunity),
      riskInCustody = RoshRiskWidgetDto.mapRiskLevelsToStrings(riskInCustody),
    )
  }

  private fun calculateWidgetOverallRiskLevel(riskInCommunity: Map<String, RiskLevel?>, riskInCustody: Map<String, RiskLevel?>): RiskLevel? {
    val riskInCommunityGroup = riskInCommunity.asGroups()
    val riskInCustodyGroup = riskInCustody.asGroups()

    return calculateOverallRiskLevel(riskInCommunityGroup + riskInCustodyGroup)
  }

  private fun Map<String, RiskLevel?>.asGroups(): Map<RiskLevel?, List<String>> {
    return this.map { it.key to it.value }.groupBy({ it.second }, { it.first }).filterKeys { it != null }
  }

  private fun Collection<QuestionAnswerDto>?.toRiskInCustodyMap(): Map<String, RiskLevel?> {
    val children = RiskLevel.fromString(findAnswer(RoshQuestionCodes.CHILDREN_IN_CUSTODY_RISK, this)?.staticText)
    val public = RiskLevel.fromString(findAnswer(RoshQuestionCodes.PUBLIC_IN_CUSTODY_RISK, this)?.staticText)
    val knowAdult = RiskLevel.fromString(findAnswer(RoshQuestionCodes.KNOWN_ADULT_IN_CUSTODY_RISK, this)?.staticText)
    val staff = RiskLevel.fromString(findAnswer(RoshQuestionCodes.STAFF_IN_CUSTODY_RISK, this)?.staticText)
    val prisoners = RiskLevel.fromString(findAnswer(RoshQuestionCodes.PRISONERS_IN_CUSTODY_RISK, this)?.staticText)

    return mapOf(
      "Children" to children,
      "Public" to public,
      "Known Adult" to knowAdult,
      "Staff" to staff,
      "Prisoners" to prisoners,
    )
  }

  private fun Collection<QuestionAnswerDto>?.toRiskInCommunityMap(): Map<String, RiskLevel?> {
    val children = RiskLevel.fromString(findAnswer(RoshQuestionCodes.CHILDREN_IN_COMMUNITY_RISK, this)?.staticText)
    val public = RiskLevel.fromString(findAnswer(RoshQuestionCodes.PUBLIC_IN_COMMUNITY_RISK, this)?.staticText)
    val knowAdult = RiskLevel.fromString(findAnswer(RoshQuestionCodes.KNOWN_ADULT_IN_COMMUNITY_RISK, this)?.staticText)
    val staff = RiskLevel.fromString(findAnswer(RoshQuestionCodes.STAFF_IN_COMMUNITY_RISK, this)?.staticText)

    return mapOf(
      "Children" to children,
      "Public" to public,
      "Known Adult" to knowAdult,
      "Staff" to staff,
    )
  }
}
