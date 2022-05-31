package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import org.apache.commons.lang3.StringUtils
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysPredictorsDto
import java.time.LocalDateTime

class RiskScoresDto(
  val completedDate: LocalDateTime? = null,
  val assessmentStatus: String? = null,
  val groupReconvictionScore: OgrScoreDto? = null,
  val violencePredictorScore: OvpScoreDto? = null,
  val generalPredictorScore: OgpScoreDto? = null,
  val riskOfSeriousRecidivismScore: RsrScoreDto? = null,
  val sexualPredictorScore: OspScoreDto? = null
) {

  companion object {
    fun from(predictorsDtos: List<OasysPredictorsDto>?): List<RiskScoresDto> {
      return predictorsDtos?.map { from(it) }.orEmpty()
    }

    fun from(oasysPredictorsDto: OasysPredictorsDto): RiskScoresDto {
      with(oasysPredictorsDto) {
        return RiskScoresDto(
          completedDate = completedDate,
          assessmentStatus = assessmentStatus,
          groupReconvictionScore = OgrScoreDto(
            oneYear = ogr3?.ogrs3_1Year,
            twoYears = ogr3?.ogrs3_2Year,
            scoreLevel = ScoreLevel.findByType(ogr3?.reconvictionRisk?.description)
          ),
          violencePredictorScore = OvpScoreDto(
            ovpStaticWeightedScore = ovp?.ovpStaticWeightedScore,
            ovpDynamicWeightedScore = ovp?.ovpDynamicWeightedScore,
            ovpTotalWeightedScore = ovp?.ovpTotalWeightedScore,
            oneYear = ovp?.ovp1Year,
            twoYears = ovp?.ovp2Year,
            ovpRisk = ScoreLevel.findByType(ovp?.ovpRisk?.description)
          ),
          generalPredictorScore = OgpScoreDto(
            ogpStaticWeightedScore = ogp?.ogpStaticWeightedScore,
            ogpDynamicWeightedScore = ogp?.ogpDynamicWeightedScore,
            ogpTotalWeightedScore = ogp?.ogpTotalWeightedScore,
            ogp1Year = ogp?.ogp1Year,
            ogp2Year = ogp?.ogp2Year,
            ogpRisk = ScoreLevel.findByType(ogp?.ogpRisk?.description)
          ),
          riskOfSeriousRecidivismScore = RsrScoreDto(
            percentageScore = rsr?.rsrPercentageScore,
            staticOrDynamic = ScoreType.findByType(rsr?.rsrStaticOrDynamic ?: StringUtils.EMPTY),
            source = RsrScoreSource.OASYS,
            algorithmVersion = rsr?.rsrAlgorithmVersion?.toString(),
            scoreLevel = ScoreLevel.findByType(rsr?.rsrRiskRecon?.description)
          ),
          sexualPredictorScore = OspScoreDto(
            ospIndecentPercentageScore = osp?.ospIndecentPercentageScore,
            ospContactPercentageScore = osp?.ospContactPercentageScore,
            ospIndecentScoreLevel = ScoreLevel.findByType(osp?.ospIndecentRiskRecon?.description),
            ospContactScoreLevel = ScoreLevel.findByType(osp?.ospContactRiskRecon?.description)
          )
        )
      }
    }
  }
}
