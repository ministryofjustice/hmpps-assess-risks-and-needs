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
      findAnswer(roshSumAnswers, RoshQuestionCodes.WHO_IS_AT_RISK)?.freeFormText,
      findAnswer(roshSumAnswers, RoshQuestionCodes.NATURE_OF_RISK)?.freeFormText,
      findAnswer(roshSumAnswers, RoshQuestionCodes.RISK_IMMINENCE)?.freeFormText,
      findAnswer(roshSumAnswers, RoshQuestionCodes.RISK_INCREASE_FACTORS)?.freeFormText,
      findAnswer(roshSumAnswers, RoshQuestionCodes.RISK_MITIGATION_FACTORS)?.freeFormText,
      roshSumAnswers.toRiskInCommunity(),
      roshSumAnswers.toRiskInCustody()
    )
  }

  private fun Collection<QuestionAnswerDto>?.toRiskInCustody(): Map<RiskLevel, List<String>> {
    val children = RiskLevel.fromString(findAnswer(this, RoshQuestionCodes.CHILDREN_IN_CUSTODY_RISK)?.staticText)
    val public = RiskLevel.fromString(findAnswer(this, RoshQuestionCodes.PUBLIC_IN_CUSTODY_RISK)?.staticText)
    val knowAdult = RiskLevel.fromString(findAnswer(this, RoshQuestionCodes.KNOWN_ADULT_IN_CUSTODY_RISK)?.staticText)
    val staff = RiskLevel.fromString(findAnswer(this, RoshQuestionCodes.STAFF_IN_CUSTODY_RISK)?.staticText)
    val prisoners = RiskLevel.fromString(findAnswer(this, RoshQuestionCodes.PRISONERS_IN_CUSTODY_RISK)?.staticText)

    return listOf(
      "Children" to children,
      "Public" to public,
      "Known Adult" to knowAdult,
      "Staff" to staff,
      "Prisoners" to prisoners
    ).groupBy({ it.second }, { it.first })
  }

  private fun Collection<QuestionAnswerDto>?.toRiskInCommunity(): Map<RiskLevel, List<String>> {
    val children = RiskLevel.fromString(findAnswer(this, RoshQuestionCodes.CHILDREN_IN_COMMUNITY_RISK)?.staticText)
    val public = RiskLevel.fromString(findAnswer(this, RoshQuestionCodes.PUBLIC_IN_COMMUNITY_RISK)?.staticText)
    val knowAdult = RiskLevel.fromString(findAnswer(this, RoshQuestionCodes.KNOWN_ADULT_IN_COMMUNITY_RISK)?.staticText)
    val staff = RiskLevel.fromString(findAnswer(this, RoshQuestionCodes.STAFF_IN_COMMUNITY_RISK)?.staticText)
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
      ResponseDto.fromString(findAnswer(roshAnswers, RoshQuestionCodes.ESCAPE_OR_ABSCOND)?.staticText),
      ResponseDto.fromString(findAnswer(roshAnswers, RoshQuestionCodes.CONTROL_ISSUES_DISRUPTIVE_BEHAVIOUR)?.staticText),
      ResponseDto.fromString(findAnswer(roshAnswers, RoshQuestionCodes.BREACH_OF_TRUST)?.staticText),
      ResponseDto.fromString(findAnswer(roshAnswers, RoshQuestionCodes.RISK_TO_OTHER_PRISONERS)?.staticText),
    )
  }

  private fun roshRiskToSelfDto(
    sectionsAnswers: SectionAnswersDto?
  ): RoshRiskToSelfDto {
    val roshFullAnswers = sectionsAnswers?.sections?.get(SectionHeader.ROSH_FULL_ANALYSIS.value)
    val roshAnswers = sectionsAnswers?.sections?.get(SectionHeader.ROSH_SCREENING.value)
    return RoshRiskToSelfDto(
      RiskDto(
        ResponseDto.fromString(findAnswer(roshAnswers, RoshQuestionCodes.SUICIDE_RISK)?.staticText),
        ResponseDto.fromString(findAnswer(roshFullAnswers, RoshQuestionCodes.SUICIDE_PREVIOUS_RISK)?.staticText),
        ResponseDto.fromString(findAnswer(roshFullAnswers, RoshQuestionCodes.SUICIDE_CURRENT_RISK)?.staticText)
      ),
      RiskDto(
        ResponseDto.fromString(findAnswer(roshAnswers, RoshQuestionCodes.SELF_HARM_RISK)?.staticText),
        ResponseDto.fromString(findAnswer(roshFullAnswers, RoshQuestionCodes.SELF_HARM_PREVIOUS_RISK)?.staticText),
        ResponseDto.fromString(findAnswer(roshFullAnswers, RoshQuestionCodes.SELF_HARM_CURRENT_RISK)?.staticText)
      ),
      RiskDto(
        ResponseDto.fromString(findAnswer(roshAnswers, RoshQuestionCodes.CUSTODY_RISK)?.staticText),
        ResponseDto.fromString(findAnswer(roshFullAnswers, RoshQuestionCodes.CUSTODY_PREVIOUS_RISK)?.staticText),
        ResponseDto.fromString(findAnswer(roshFullAnswers, RoshQuestionCodes.CUSTODY_CURRENT_RISK)?.staticText)
      ),
      RiskDto(
        ResponseDto.fromString(findAnswer(roshAnswers, RoshQuestionCodes.CUSTODY_RISK)?.staticText),
        ResponseDto.fromString(findAnswer(roshFullAnswers, RoshQuestionCodes.HOSTEL_SETTING_PREVIOUS_RISK)?.staticText),
        ResponseDto.fromString(findAnswer(roshFullAnswers, RoshQuestionCodes.HOSTEL_SETTING_CURRENT_RISK)?.staticText)
      ),
      RiskDto(
        ResponseDto.fromString(findAnswer(roshAnswers, RoshQuestionCodes.VULNERABILITY_RISK)?.staticText),
        ResponseDto.fromString(findAnswer(roshFullAnswers, RoshQuestionCodes.VULNERABILITY_PREVIOUS_RISK)?.staticText),
        ResponseDto.fromString(findAnswer(roshFullAnswers, RoshQuestionCodes.VULNERABILITY_CURRENT_RISK)?.staticText)
      )
    )
  }

  private fun findAnswer(answers: Collection<QuestionAnswerDto>?, questionCode: RoshQuestionCodes): QuestionAnswerDto? {
    return answers?.find { q -> q.refQuestionCode.equals(questionCode.value) }
  }
}
