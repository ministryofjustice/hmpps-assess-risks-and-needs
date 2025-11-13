package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysRiskPredictorsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.RiskPredictorAssessmentDto
import java.time.LocalDateTime

class RiskScoresDto(
  val completedDate: LocalDateTime? = null,
  val assessmentStatus: String? = null,
  val groupReconvictionScore: OgrScoreDto? = null,
  val violencePredictorScore: OvpScoreDto? = null,
  val generalPredictorScore: OgpScoreDto? = null,
  val riskOfSeriousRecidivismScore: RsrScoreDto? = null,
  val sexualPredictorScore: OspScoreDto? = null,
) {

  companion object {

    fun from(oasysRiskPredictorsDto: OasysRiskPredictorsDto?): List<RiskScoresDto> = oasysRiskPredictorsDto?.assessments?.filter { it.assessmentType in listOf("LAYER3", "LAYER1") }?.map {
      RiskScoresDto(
        completedDate = it.dateCompleted,
        assessmentStatus = it.assessmentStatus.name,
        groupReconvictionScore = OgrScoreDto(
          oneYear = it.ogrScoreDto.ogrs31Year,
          twoYears = it.ogrScoreDto.ogrs32Year,
          scoreLevel = ScoreLevel.findByType(it.ogrScoreDto.ogrs3RiskRecon),
        ),
        violencePredictorScore = OvpScoreDto(
          ovpStaticWeightedScore = it.ovpScoreDto.ovpStWesc,
          ovpDynamicWeightedScore = it.ovpScoreDto.ovpDyWesc,
          ovpTotalWeightedScore = it.ovpScoreDto.ovpTotWesc,
          oneYear = it.ovpScoreDto.ovp1Year,
          twoYears = it.ovpScoreDto.ovp2Year,
          ovpRisk = ScoreLevel.findByType(it.ovpScoreDto.ovpRisk),
        ),
        generalPredictorScore = OgpScoreDto(
          ogpStaticWeightedScore = it.ogpScoreDto.ogpStWesc,
          ogpDynamicWeightedScore = it.ogpScoreDto.ogpDyWesc,
          ogpTotalWeightedScore = it.ogpScoreDto.ogpTotWesc,
          ogp1Year = it.ogpScoreDto.ogp1Year,
          ogp2Year = it.ogpScoreDto.ogp2Year,
          ogpRisk = ScoreLevel.findByType(it.ogpScoreDto.ogpRisk),
        ),
        riskOfSeriousRecidivismScore = RsrScoreDto(
          percentageScore = it.rsrScoreDto.rsrPercentageScore,
          staticOrDynamic = it.rsrScoreDto.rsrStaticOrDynamic,
          source = RsrScoreSource.OASYS,
          algorithmVersion = it.rsrScoreDto.rsrAlgorithmVersion,
          scoreLevel = ScoreLevel.findByType(it.rsrScoreDto.scoreLevel),
        ),
        sexualPredictorScore = OspScoreDto(
          ospIndecentPercentageScore = it.ospScoreDto.ospImagePercentageScore,
          ospContactPercentageScore = it.ospScoreDto.ospContactPercentageScore,
          ospIndecentScoreLevel = ScoreLevel.findByType(it.ospScoreDto.ospImageScoreLevel),
          ospContactScoreLevel = ScoreLevel.findByType(it.ospScoreDto.ospContactScoreLevel),

          ospIndirectImagePercentageScore = it.ospScoreDto.ospIndirectImagesChildrenPercentageScore,
          ospDirectContactPercentageScore = it.ospScoreDto.ospDirectContactPercentageScore,
          ospIndirectImageScoreLevel = ScoreLevel.findByType(it.ospScoreDto.ospIndirectImagesChildrenScoreLevel),
          ospDirectContactScoreLevel = ScoreLevel.findByType(it.ospScoreDto.ospDirectContactScoreLevel),
        ),
      )
    }.orEmpty()

    fun fromVersioned(assessment: RiskPredictorAssessmentDto): RiskScoresDto = RiskScoresDto(
      groupReconvictionScore = OgrScoreDto(
        oneYear = assessment.ogrScoreDto.ogrs31Year,
        twoYears = assessment.ogrScoreDto.ogrs32Year,
        scoreLevel = ScoreLevel.findByType(assessment.ogrScoreDto.ogrs3RiskRecon),
      ),
      violencePredictorScore = OvpScoreDto(
        ovpStaticWeightedScore = assessment.ovpScoreDto.ovpStWesc,
        ovpDynamicWeightedScore = assessment.ovpScoreDto.ovpDyWesc,
        ovpTotalWeightedScore = assessment.ovpScoreDto.ovpTotWesc,
        oneYear = assessment.ovpScoreDto.ovp1Year,
        twoYears = assessment.ovpScoreDto.ovp2Year,
        ovpRisk = ScoreLevel.findByType(assessment.ovpScoreDto.ovpRisk),
      ),
      generalPredictorScore = OgpScoreDto(
        ogpStaticWeightedScore = assessment.ogpScoreDto.ogpStWesc,
        ogpDynamicWeightedScore = assessment.ogpScoreDto.ogpDyWesc,
        ogpTotalWeightedScore = assessment.ogpScoreDto.ogpTotWesc,
        ogp1Year = assessment.ogpScoreDto.ogp1Year,
        ogp2Year = assessment.ogpScoreDto.ogp2Year,
        ogpRisk = ScoreLevel.findByType(assessment.ogpScoreDto.ogpRisk),
      ),
      riskOfSeriousRecidivismScore = RsrScoreDto(
        percentageScore = assessment.rsrScoreDto.rsrPercentageScore,
        staticOrDynamic = assessment.rsrScoreDto.rsrStaticOrDynamic,
        source = RsrScoreSource.OASYS,
        algorithmVersion = assessment.rsrScoreDto.rsrAlgorithmVersion,
        scoreLevel = ScoreLevel.findByType(assessment.rsrScoreDto.scoreLevel),
      ),
      sexualPredictorScore = OspScoreDto(
        ospIndecentPercentageScore = assessment.ospScoreDto.ospImagePercentageScore,
        ospContactPercentageScore = assessment.ospScoreDto.ospContactPercentageScore,
        ospIndecentScoreLevel = ScoreLevel.findByType(assessment.ospScoreDto.ospImageScoreLevel),
        ospContactScoreLevel = ScoreLevel.findByType(assessment.ospScoreDto.ospContactScoreLevel),

        ospIndirectImagePercentageScore = assessment.ospScoreDto.ospIndirectImagesChildrenPercentageScore,
        ospDirectContactPercentageScore = assessment.ospScoreDto.ospDirectContactPercentageScore,
        ospIndirectImageScoreLevel = ScoreLevel.findByType(assessment.ospScoreDto.ospIndirectImagesChildrenScoreLevel),
        ospDirectContactScoreLevel = ScoreLevel.findByType(assessment.ospScoreDto.ospDirectContactScoreLevel),
      ),
    )
  }
}
