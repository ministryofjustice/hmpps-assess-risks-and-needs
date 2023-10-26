package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import uk.gov.justice.digital.hmpps.assessrisksandneeds.jpa.entities.OffenderPredictorsHistoryEntity
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysPredictorsDto
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

    fun from(oasysPredictorsDtos: List<OasysPredictorsDto>): List<RsrPredictorDto> {
      return oasysPredictorsDtos.map { from(it) }
    }

    fun from(oasysPredictorsDto: OasysPredictorsDto): RsrPredictorDto {
      with(oasysPredictorsDto) {
        return RsrPredictorDto(
          rsrPercentageScore = rsr?.rsrPercentageScore,
          rsrScoreLevel = ScoreLevel.findByType(rsr?.rsrRiskRecon?.description!!),
          ospcPercentageScore = osp?.ospContactPercentageScore,
          ospcScoreLevel = osp?.ospContactRiskRecon?.description?.let { ScoreLevel.findByType(it) },
          ospiPercentageScore = osp?.ospIndecentPercentageScore,
          ospiScoreLevel = osp?.ospIndecentRiskRecon?.description?.let { ScoreLevel.findByType(it) },
          calculatedDate = null,
          completedDate = completedDate,
          signedDate = null,
          staticOrDynamic = ScoreType.valueOf(rsr.rsrStaticOrDynamic?.uppercase()!!),
          source = RsrScoreSource.OASYS,
          status = AssessmentStatus.valueOf(assessmentStatus!!),
          algorithmVersion = rsr.rsrAlgorithmVersion.toString(),
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
