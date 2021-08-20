package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OffenderAndOffencesDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PredictorType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskPredictorsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.Score
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.config.RequestData
import uk.gov.justice.digital.hmpps.assessrisksandneeds.jpa.entities.OffenderPredictorsHistoryEntity
import uk.gov.justice.digital.hmpps.assessrisksandneeds.jpa.respositories.OffenderPredictorsHistoryRepository
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.AssessmentApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.OasysRSRPredictorsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.PredictorCalculationError

@Service
class RiskPredictorService(
  private val assessmentClient: AssessmentApiRestClient,
  private val offenderPredictorsHistoryRepository: OffenderPredictorsHistoryRepository
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun calculatePredictorScores(
    predictorType: PredictorType,
    offenderAndOffences: OffenderAndOffencesDto,
    final: Boolean,
    source: String,
    sourceId: String
  ): RiskPredictorsDto {
    val errorMessage =
      "Oasys Predictor Calculation failed for offender with CRN ${offenderAndOffences.crn} and $predictorType"
    val predictorCalculation =
      assessmentClient.calculatePredictorTypeScoring(predictorType, offenderAndOffences)
        ?: throw PredictorCalculationError(errorMessage)

    if (predictorCalculation.errorCount > 0) log.info("$errorMessage - ${predictorCalculation.errorMessage}")
    val predictors = predictorCalculation.toRiskPredictorsDto(predictorType)
    if (final) {
      offenderPredictorsHistoryRepository.save(
        predictors.toOffenderPredictorsHistory(
          offenderAndOffences.crn,
          source,
          sourceId
        )
      )
    }
    return predictors
  }

  private fun OasysRSRPredictorsDto.toRiskPredictorsDto(predictorType: PredictorType): RiskPredictorsDto {
    return when (predictorType) {
      PredictorType.RSR -> {
        RiskPredictorsDto(
          algorithmVersion = this.algorithmVersion.toString(),
          calculatedAt = this.calculationDateAndTime,
          type = predictorType,
          scoreType = ScoreType.findByType(this.scoreType!!),
          rsrScore = Score(
            level = ScoreLevel.findByType(this.rsrBand!!),
            score = this.rsrScore,
            isValid = this.validRsrScore.toBoolean()
          ),
          ospcScore = Score(
            level = ScoreLevel.findByType(this.ospcBand!!),
            score = this.ospcScore,
            isValid = this.validOspcScore.toBoolean()
          ),
          ospiScore = Score(
            level = ScoreLevel.findByType(this.ospiBand!!),
            score = this.ospiScore,
            isValid = this.validOspiScore.toBoolean()
          ),
          errors = this.toErrors()
        )
      }
    }
  }

  private fun String?.toBoolean(): Boolean {
    return this?.equals(AnswerType.Y.name) == true
  }

  private fun OasysRSRPredictorsDto.toErrors(): List<String> {
    return if (this.errorCount > 0) this.errorMessage?.split('\n')?.filter { !it.isNullOrBlank() }
      ?: emptyList() else emptyList()
  }

  enum class AnswerType {
    Y, N
  }
}

private fun RiskPredictorsDto.toOffenderPredictorsHistory(
  crn: String,
  source: String,
  sourceId: String
): OffenderPredictorsHistoryEntity {
  return OffenderPredictorsHistoryEntity(
    predictorType = this.type,
    algorithmVersion = this.algorithmVersion,
    calculatedAt = this.calculatedAt,
    crn = crn,
    predictorTriggerSource = source,
    predictorTriggerSourceId = sourceId,
    createdBy = RequestData.getUserName()
  )
}
