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
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.QuestionAnswerDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.SectionAnswersDto

@Service
class RiskService(private val assessmentClient: AssessmentApiRestClient) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun getRoshRisksByCrn(crn: String): AllRoshRiskDto {
    log.info("Get Rosh Risk for crn $crn")
    val sectionsAnswers = assessmentClient.getRoshSectionsForCompletedLastYearAssessment(crn)
    log.info("Section answers for crn $crn number of sections : ${sectionsAnswers?.sections?.size}")
    return AllRoshRiskDto(
      roshRiskToSelfDto(sectionsAnswers),
      sectionsAnswers.toOtherRoshRisksDto(),
      sectionsAnswers.toRiskRoshSummaryDto()
    )
  }

  fun getOtherRoshRisk(crn: String): OtherRoshRisksDto {
    log.info("Get Other Rosh Risk for crn $crn")
    val sectionsAnswers = assessmentClient.getRoshSectionsForCompletedLastYearAssessment(crn)
    log.info("Section answers for crn $crn number of sections : ${sectionsAnswers?.sections?.size}")
    return sectionsAnswers.toOtherRoshRisksDto()
  }

  fun getRoshRisksToSelfByCrn(crn: String): RoshRiskToSelfDto {
    log.info("Get Rosh Risk to Self for crn $crn")
    val sectionsAnswers = assessmentClient.getRoshSectionsForCompletedLastYearAssessment(crn)
    log.info("Section answers for crn $crn number of sections : ${sectionsAnswers?.sections?.size}")

    return roshRiskToSelfDto(sectionsAnswers)
  }

  fun getRoshRiskSummaryByCrn(crn: String): RiskRoshSummaryDto {
    log.info("Get Rosh Risk summary for crn $crn")
    val sectionsAnswers = assessmentClient.getRoshSectionsForCompletedLastYearAssessment(crn)
    log.info("Section answers for crn $crn number of sections : ${sectionsAnswers?.sections?.size}")
    return sectionsAnswers.toRiskRoshSummaryDto()
  }

  private fun SectionAnswersDto?.toRiskRoshSummaryDto(): RiskRoshSummaryDto {
    val roshSumAnswers = this?.sections?.get(SectionHeader.ROSH_SUMMARY.value)

    return RiskRoshSummaryDto(
      findAnswer(RoshQuestionCodes.WHO_IS_AT_RISK, roshSumAnswers)?.freeFormText,
      findAnswer(RoshQuestionCodes.NATURE_OF_RISK, roshSumAnswers)?.freeFormText,
      findAnswer(RoshQuestionCodes.RISK_IMMINENCE, roshSumAnswers)?.freeFormText,
      findAnswer(RoshQuestionCodes.RISK_INCREASE_FACTORS, roshSumAnswers)?.freeFormText,
      findAnswer(RoshQuestionCodes.RISK_MITIGATION_FACTORS, roshSumAnswers)?.freeFormText,
      roshSumAnswers.toRiskInCommunity(),
      roshSumAnswers.toRiskInCustody()
    )
  }

  private fun Collection<QuestionAnswerDto>?.toRiskInCustody(): Map<RiskLevel, List<String>> {
    val children = RiskLevel.fromString(findAnswer(RoshQuestionCodes.CHILDREN_IN_CUSTODY_RISK, this)?.staticText)
    val public = RiskLevel.fromString(findAnswer(RoshQuestionCodes.PUBLIC_IN_CUSTODY_RISK, this)?.staticText)
    val knowAdult = RiskLevel.fromString(findAnswer(RoshQuestionCodes.KNOWN_ADULT_IN_CUSTODY_RISK, this)?.staticText)
    val staff = RiskLevel.fromString(findAnswer(RoshQuestionCodes.STAFF_IN_CUSTODY_RISK, this)?.staticText)
    val prisoners = RiskLevel.fromString(findAnswer(RoshQuestionCodes.PRISONERS_IN_CUSTODY_RISK, this)?.staticText)

    return listOf(
      "Children" to children,
      "Public" to public,
      "Known Adult" to knowAdult,
      "Staff" to staff,
      "Prisoners" to prisoners
    ).groupBy({ it.second }, { it.first })
  }

  private fun Collection<QuestionAnswerDto>?.toRiskInCommunity(): Map<RiskLevel, List<String>> {
    val children = RiskLevel.fromString(findAnswer(RoshQuestionCodes.CHILDREN_IN_COMMUNITY_RISK, this)?.staticText)
    val public = RiskLevel.fromString(findAnswer(RoshQuestionCodes.PUBLIC_IN_COMMUNITY_RISK, this)?.staticText)
    val knowAdult = RiskLevel.fromString(findAnswer(RoshQuestionCodes.KNOWN_ADULT_IN_COMMUNITY_RISK, this)?.staticText)
    val staff = RiskLevel.fromString(findAnswer(RoshQuestionCodes.STAFF_IN_COMMUNITY_RISK, this)?.staticText)
    return listOf(
      "Children" to children,
      "Public" to public,
      "Known Adult" to knowAdult,
      "Staff" to staff
    ).groupBy({ it.second }, { it.first })
  }

  private fun SectionAnswersDto?.toOtherRoshRisksDto(): OtherRoshRisksDto {
    val roshAnswers = this?.sections?.get(SectionHeader.ROSH_SCREENING.value)
    return OtherRoshRisksDto(
      ResponseDto.fromString(findAnswer(RoshQuestionCodes.ESCAPE_OR_ABSCOND, roshAnswers)?.staticText),
      ResponseDto.fromString(
        findAnswer(
          RoshQuestionCodes.CONTROL_ISSUES_DISRUPTIVE_BEHAVIOUR,
          roshAnswers
        )?.staticText
      ),
      ResponseDto.fromString(findAnswer(RoshQuestionCodes.BREACH_OF_TRUST, roshAnswers)?.staticText),
      ResponseDto.fromString(findAnswer(RoshQuestionCodes.RISK_TO_OTHER_PRISONERS, roshAnswers)?.staticText),
    )
  }

  private fun roshRiskToSelfDto(
    sectionsAnswers: SectionAnswersDto?
  ): RoshRiskToSelfDto {
    val roshFullAnswers = sectionsAnswers?.sections?.get(SectionHeader.ROSH_FULL_ANALYSIS.value)
    val roshAnswers = sectionsAnswers?.sections?.get(SectionHeader.ROSH_SCREENING.value)
    val selfHarmCurrentRisk = getResponseForQuestion(roshFullAnswers, RoshQuestionCodes.SELF_HARM_CURRENT_RISK)
    val suicideCurrentRisk = getResponseForQuestion(roshFullAnswers, RoshQuestionCodes.SUICIDE_CURRENT_RISK)
    val custodyCurrentRisk = getResponseForQuestion(roshFullAnswers, RoshQuestionCodes.CUSTODY_CURRENT_RISK)
    val hostelSettingCurrentRisk =
      getResponseForQuestion(roshFullAnswers, RoshQuestionCodes.HOSTEL_SETTING_CURRENT_RISK)
    val vulnerabilityCurrentRisk = getResponseForQuestion(roshFullAnswers, RoshQuestionCodes.VULNERABILITY_CURRENT_RISK)
    return RoshRiskToSelfDto(
      suicide = RiskDto(
        risk = getResponseForQuestion(roshAnswers, RoshQuestionCodes.SUICIDE_RISK),
        previous = getResponseForQuestion(roshFullAnswers, RoshQuestionCodes.SUICIDE_PREVIOUS_RISK),
        current = suicideCurrentRisk,
        currentConcernsText = getConcernsFreeText(
          suicideCurrentRisk,
          roshFullAnswers,
          RoshQuestionCodes.SUICIDE_SELF_HARM_CURRENT_RISK_TEXT
        )
      ),
      selfHarm = RiskDto(
        risk = getResponseForQuestion(roshAnswers, RoshQuestionCodes.SELF_HARM_RISK),
        previous = getResponseForQuestion(roshFullAnswers, RoshQuestionCodes.SELF_HARM_PREVIOUS_RISK),
        current = selfHarmCurrentRisk,
        currentConcernsText = getConcernsFreeText(
          selfHarmCurrentRisk,
          roshFullAnswers,
          RoshQuestionCodes.SUICIDE_SELF_HARM_CURRENT_RISK_TEXT
        )
      ),
      custody = RiskDto(
        risk = getResponseForQuestion(roshAnswers, RoshQuestionCodes.CUSTODY_RISK),
        previous = getResponseForQuestion(roshFullAnswers, RoshQuestionCodes.CUSTODY_PREVIOUS_RISK),
        current = custodyCurrentRisk,
        currentConcernsText = getConcernsFreeText(
          custodyCurrentRisk,
          roshFullAnswers,
          RoshQuestionCodes.CUSTODY_HOSTEL_SETTING_CURRENT_RISK_TEXT
        )
      ),
      hostelSetting = RiskDto(
        risk = getResponseForQuestion(roshAnswers, RoshQuestionCodes.CUSTODY_RISK),
        previous = getResponseForQuestion(roshFullAnswers, RoshQuestionCodes.HOSTEL_SETTING_PREVIOUS_RISK),
        current = hostelSettingCurrentRisk,
        currentConcernsText = getConcernsFreeText(
          hostelSettingCurrentRisk,
          roshFullAnswers,
          RoshQuestionCodes.CUSTODY_HOSTEL_SETTING_CURRENT_RISK_TEXT
        )
      ),
      vulnerability = RiskDto(
        risk = getResponseForQuestion(roshAnswers, RoshQuestionCodes.VULNERABILITY_RISK),
        previous = getResponseForQuestion(roshFullAnswers, RoshQuestionCodes.VULNERABILITY_PREVIOUS_RISK),
        current = vulnerabilityCurrentRisk,
        currentConcernsText = getConcernsFreeText(
          vulnerabilityCurrentRisk,
          roshFullAnswers,
          RoshQuestionCodes.VULNERABILITY_CURRENT_RISK_TEXT
        )
      )
    )
  }

  private fun getResponseForQuestion(answers: Collection<QuestionAnswerDto>?, questionCode: RoshQuestionCodes) =
    ResponseDto.fromString(
      findAnswer(
        questionCode,
        answers
      )?.staticText
    )

  private fun getConcernsFreeText(
    riskResponse: ResponseDto?,
    answers: Collection<QuestionAnswerDto>?,
    questionCode: RoshQuestionCodes
  ) = if (riskResponse == ResponseDto.YES) {
    findAnswer(questionCode, answers)?.freeFormText
  } else null

  private fun findAnswer(questionCode: RoshQuestionCodes, answers: Collection<QuestionAnswerDto>?): QuestionAnswerDto? {
    return answers?.find { q -> q.refQuestionCode.equals(questionCode.value) }
  }
}
