package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OffenderAndOffencesDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PredictorSubType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PredictorType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskPredictorsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.Score
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.AssessmentApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysRSRPredictorsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.PredictorCalculationError

@Profile("oasys-rsr")
@Service
class OASysCalculatorServiceImpl(
  private val assessmentClient: AssessmentApiRestClient,
) : RiskCalculatorService {

  override fun calculatePredictorScores(
    predictorType: PredictorType,
    offenderAndOffences: OffenderAndOffencesDto,
    algorithmVersion: String?
  ): RiskPredictorsDto {

    val errorMessage =
      "Oasys Predictor Calculation failed for offender with CRN ${offenderAndOffences.crn} and $predictorType"

    val predictorCalculation =
      assessmentClient.calculatePredictorTypeScoring(predictorType, offenderAndOffences, algorithmVersion)
        ?: throw PredictorCalculationError(errorMessage)

    if (predictorCalculation.errorCount > 0) RiskPredictorService.log.error("$errorMessage - ${predictorCalculation.errorMessage}")
    return predictorCalculation.toRiskPredictorsDto(predictorType)
  }

  private fun OasysRSRPredictorsDto.toRiskPredictorsDto(predictorType: PredictorType): RiskPredictorsDto {
    return when (predictorType) {
      PredictorType.RSR -> {
        RiskPredictorsDto(
          algorithmVersion = this.algorithmVersion.toString(),
          calculatedAt = this.calculationDateAndTime,
          type = predictorType,
          scoreType = ScoreType.findByType(this.scoreType!!),
          scores = mapOf(
            PredictorSubType.RSR to Score(
              level = ScoreLevel.findByType(this.rsrBand!!),
              score = this.rsrScore,
              isValid = this.validRsrScore.toBoolean()
            ),
            PredictorSubType.OSPC to Score(
              level = ScoreLevel.findByType(this.ospcBand!!),
              score = this.ospcScore,
              isValid = this.validOspcScore.toBoolean()
            ),
            PredictorSubType.OSPI to Score(
              level = ScoreLevel.findByType(this.ospiBand!!),
              score = this.ospiScore,
              isValid = this.validOspiScore.toBoolean()
            ),
          ),
          errors = this.toErrors(),
          errorCount = this.errorCount
        )
      }
    }
  }

  enum class AnswerType {
    Y, N
  }

  private fun String?.toBoolean(): Boolean {
    return this?.equals(AnswerType.Y.name) == true
  }

  private fun OasysRSRPredictorsDto.toErrors(): List<String> {
    return if (this.errorCount > 0) this.errorMessage?.split('\n')?.filter { !it.isNullOrBlank() }
      ?: emptyList() else emptyList()
  }
}
