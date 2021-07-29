package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OffenderAndOffencesDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PredictorType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskPredictorsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.Score
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.AssessmentApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.OasysRSRPredictorsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.PredictorCalculationError

@Service
class RiskPredictorService(private val assessmentClient: AssessmentApiRestClient) {
  fun getPredictorScores(
    predictorType: PredictorType,
    offenderAndOffences: OffenderAndOffencesDto
  ): RiskPredictorsDto {
    val predictorCalculation =
      assessmentClient.calculatePredictorTypeScoring(predictorType, offenderAndOffences)
        ?: throw PredictorCalculationError("Oasys Predictor for offender with CRN ${offenderAndOffences.crn} for $predictorType has failed")

    return predictorCalculation?.toRiskPredictorsDto(predictorType)
  }

  private fun OasysRSRPredictorsDto.toRiskPredictorsDto(predictorType: PredictorType): RiskPredictorsDto {
    return when (predictorType) {
      PredictorType.RSR -> {
        RiskPredictorsDto(
          algorithmVersion = this.algorithmVersion,
          type = predictorType,
          scoreType = ScoreType.findByType(this?.scoreType!!),
          rsrScore = Score(
            level = ScoreLevel.findByType(this?.rsrBand!!),
            score = this?.rsrScore,
            isValid = this?.validRsrScore.toBoolean()
          ),
          ospcScore = Score(
            level = ScoreLevel.findByType(this?.ospcBand!!),
            score = this?.ospcScore,
            isValid = this?.validOspcScore.toBoolean()
          ),
          ospiScore = Score(
            level = ScoreLevel.findByType(this?.ospiBand!!),
            score = this?.ospiScore,
            isValid = this?.validOspiScore.toBoolean()
          ),
        )
      }
    }
  }

  private fun String?.toBoolean(): Boolean {
    return this?.equals(AnswerType.Y.name) == true
  }

  enum class AnswerType {
    Y, N
  }
}
