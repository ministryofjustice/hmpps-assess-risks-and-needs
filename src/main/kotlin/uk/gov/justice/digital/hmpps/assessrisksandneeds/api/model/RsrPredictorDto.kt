package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import uk.gov.justice.digital.hmpps.assessrisksandneeds.jpa.entities.OffenderPredictorsHistoryEntity
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.RiskPredictorAssessmentDto
import java.math.BigDecimal
import java.time.LocalDateTime

data class RsrPredictorDto(
  val rsrPercentageScore: BigDecimal? = null,
  val rsrScoreLevel: ScoreLevel? = null,
  val ospcPercentageScore: BigDecimal? = null,
  val ospcScoreLevel: ScoreLevel? = null,
  val ospiPercentageScore: BigDecimal? = null,
  val ospiScoreLevel: ScoreLevel? = null,
  val calculatedDate: LocalDateTime? = null,
  val completedDate: LocalDateTime? = null,
  val signedDate: LocalDateTime? = null,
  val staticOrDynamic: ScoreType? = null,
  val source: RsrScoreSource,
  val status: AssessmentStatus,
  val algorithmVersion: String? = null,
) {

  companion object {

    fun from(oasysPredictorsDtos: List<RiskPredictorAssessmentDto>): List<RsrPredictorDto> {
      return oasysPredictorsDtos.map { from(it) }
    }

    fun from(oasysPredictorsDto: RiskPredictorAssessmentDto): RsrPredictorDto {
      with(oasysPredictorsDto) {
        return RsrPredictorDto(
          rsrPercentageScore = rsrScoreDto.rsrPercentageScore,
          rsrScoreLevel = ScoreLevel.findByType(rsrScoreDto.scoreLevel),
          ospcPercentageScore = ospScoreDto.ospContactPercentageScore,
          ospcScoreLevel = ospScoreDto.ospContactScoreLevel?.let { ScoreLevel.findByType(it) },
          ospiPercentageScore = ospScoreDto.ospImagePercentageScore,
          ospiScoreLevel = ospScoreDto.ospImageScoreLevel?.let { ScoreLevel.findByType(it) },
          calculatedDate = null,
          completedDate = dateCompleted,
          signedDate = null,
          staticOrDynamic = rsrScoreDto.rsrStaticOrDynamic,
          source = RsrScoreSource.OASYS,
          status = assessmentStatus,
          algorithmVersion = rsrScoreDto.rsrAlgorithmVersion,
        )
      }
    }

    @JvmName("fromArn")
    fun from(arnPredictorDtos: List<OffenderPredictorsHistoryEntity>): List<RsrPredictorDto> {
      return arnPredictorDtos.map { from(it) }
    }

    fun from(arnPredictor: OffenderPredictorsHistoryEntity): RsrPredictorDto {
      with(arnPredictor) {
        val rsr = predictors.firstOrNull { it.predictorSubType == PredictorSubType.RSR }
        val ospi = predictors.firstOrNull { it.predictorSubType == PredictorSubType.OSPI }
        val ospc = predictors.firstOrNull { it.predictorSubType == PredictorSubType.OSPC }
        return RsrPredictorDto(
          rsrPercentageScore = rsr?.predictorScore,
          rsrScoreLevel = rsr?.predictorLevel,
          ospcPercentageScore = ospc?.predictorScore,
          ospcScoreLevel = ospc?.predictorLevel,
          ospiPercentageScore = ospi?.predictorScore,
          ospiScoreLevel = ospi?.predictorLevel,
          calculatedDate = calculatedAt,
          completedDate = assessmentCompletedDate,
          signedDate = null,
          staticOrDynamic = scoreType,
          source = RsrScoreSource.ASSESSMENTS_API,
          status = AssessmentStatus.COMPLETE,
          algorithmVersion = algorithmVersion,
        )
      }
    }
  }
}

enum class AssessmentStatus {
  COMPLETE, LOCKED_INCOMPLETE;
}

enum class RsrScoreSource {
  ASSESSMENTS_API, OASYS;
}
