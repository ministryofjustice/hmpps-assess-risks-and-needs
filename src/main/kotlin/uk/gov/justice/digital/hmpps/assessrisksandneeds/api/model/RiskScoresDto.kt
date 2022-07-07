package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysRiskPredictorsDto
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

    fun from(oasysRiskPredictorsDto: OasysRiskPredictorsDto?): List<RiskScoresDto> {
      return oasysRiskPredictorsDto?.assessments?.map {
        RiskScoresDto(
          completedDate = it.dateCompleted,
          assessmentStatus = it.assessmentStatus.name,
          groupReconvictionScore = OgrScoreDto(
            oneYear = it.ogrScoreDto.ogrs31Year,
            twoYears = it.ogrScoreDto.ogrs32Year,
            scoreLevel = ScoreLevel.findByType(it.ogrScoreDto.ogrs3RiskRecon)
          ),
          violencePredictorScore = OvpScoreDto(
            ovpStaticWeightedScore = it.ovpScoreDto.ovpStWesc,
            ovpDynamicWeightedScore = it.ovpScoreDto.ovpDyWesc,
            ovpTotalWeightedScore = it.ovpScoreDto.ovpTotWesc,
            oneYear = it.ovpScoreDto.ovp1Year,
            twoYears = it.ovpScoreDto.ovp2Year,
            ovpRisk = ScoreLevel.findByType(it.ovpScoreDto.ovpRisk)
          ),
          generalPredictorScore = OgpScoreDto(
            ogpStaticWeightedScore = it.ogpScoreDto.ogpStWesc,
            ogpDynamicWeightedScore = it.ogpScoreDto.ogpDyWesc,
            ogpTotalWeightedScore = it.ogpScoreDto.ogpTotWesc,
            ogp1Year = it.ogpScoreDto.ogp1Year,
            ogp2Year = it.ogpScoreDto.ogp2Year,
            ogpRisk = ScoreLevel.findByType(it.ogpScoreDto.ogpRisk)
          ),
          riskOfSeriousRecidivismScore = RsrScoreDto(
            percentageScore = it.rsrScoreDto.rsrPercentageScore,
            staticOrDynamic = it.rsrScoreDto.rsrStaticOrDynamic,
            source = RsrScoreSource.OASYS,
            algorithmVersion = it.rsrScoreDto.rsrAlgorithmVersion,
            scoreLevel = ScoreLevel.findByType(it.rsrScoreDto.scoreLevel)
          ),
          sexualPredictorScore = OspScoreDto(
            ospIndecentPercentageScore = it.ospScoreDto.ospImagePercentageScore,
            ospContactPercentageScore = it.ospScoreDto.ospContactPercentageScore,
            ospIndecentScoreLevel = ScoreLevel.findByType(it.ospScoreDto.ospImageScoreLevel),
            ospContactScoreLevel = ScoreLevel.findByType(it.ospScoreDto.ospContactScoreLevel)
          )
        )
      }.orEmpty()
    }
  }
}
