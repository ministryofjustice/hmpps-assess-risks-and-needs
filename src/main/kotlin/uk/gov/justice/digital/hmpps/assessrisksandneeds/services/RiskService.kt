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
      findAnswer(roshSumAnswers, "SUM1")?.freeFormText,
      findAnswer(roshSumAnswers, "SUM2")?.freeFormText,
      findAnswer(roshSumAnswers, "SUM3")?.freeFormText,
      findAnswer(roshSumAnswers, "SUM4")?.freeFormText,
      findAnswer(roshSumAnswers, "SUM5")?.freeFormText,
      mapOf(RiskLevel.HIGH to listOf("children")),
      mapOf(RiskLevel.MEDIUM to listOf("known adult"))
    )
  }

  private fun  SectionAnswersDto?.toOtherRoshRisksDto(): OtherRoshRisksDto {
    val roshAnswers = this?.sections?.get(SectionHeader.ROSH_SCREENING.value)
    return OtherRoshRisksDto(
      ResponseDto.fromString(findAnswer(roshAnswers, "R4.1")?.staticText),
      ResponseDto.fromString(findAnswer(roshAnswers, "R4.2")?.staticText),
      ResponseDto.fromString(findAnswer(roshAnswers, "R4.3")?.staticText),
      ResponseDto.fromString(findAnswer(roshAnswers, "R4.4")?.staticText),
    )
  }

  private fun roshRiskToSelfDto(
    sectionsAnswers: SectionAnswersDto?
  ): RoshRiskToSelfDto {
    val roshFullAnswers = sectionsAnswers?.sections?.get(SectionHeader.ROSH_FULL_ANALYSIS.value)
    val roshAnswers = sectionsAnswers?.sections?.get(SectionHeader.ROSH_SCREENING.value)
    return RoshRiskToSelfDto(
      RiskDto(
        ResponseDto.fromString(findAnswer(roshAnswers, "R3.1")?.staticText),
        ResponseDto.fromString(findAnswer(roshFullAnswers, "FA36")?.staticText),
        ResponseDto.fromString(findAnswer(roshFullAnswers, "FA31")?.staticText)
      ),
      RiskDto(
        ResponseDto.fromString(findAnswer(roshAnswers, "R3.2")?.staticText),
        ResponseDto.fromString(findAnswer(roshFullAnswers, "FA37")?.staticText),
        ResponseDto.fromString(findAnswer(roshFullAnswers, "FA32")?.staticText)
      ),
      RiskDto(
        ResponseDto.fromString(findAnswer(roshAnswers, "R3.3")?.staticText),
        ResponseDto.fromString(findAnswer(roshFullAnswers, "FA42")?.staticText),
        ResponseDto.fromString(findAnswer(roshFullAnswers, "FA39")?.staticText)
      ),
      RiskDto(
        ResponseDto.fromString(findAnswer(roshAnswers, "R3.3")?.staticText),
        ResponseDto.fromString(findAnswer(roshFullAnswers, "FA43")?.staticText),
        ResponseDto.fromString(findAnswer(roshFullAnswers, "FA40")?.staticText)
      ),
      RiskDto(
        ResponseDto.fromString(findAnswer(roshAnswers, "R3.4")?.staticText),
        ResponseDto.fromString(findAnswer(roshFullAnswers, "FA47")?.staticText),
        ResponseDto.fromString(findAnswer(roshFullAnswers, "FA45")?.staticText)
      )
    )
  }

  private fun findAnswer(answers: Collection<QuestionAnswerDto>?, question: String): QuestionAnswerDto? {
    return answers?.find { q -> q.refQuestionCode.equals(question) }
  }

}
