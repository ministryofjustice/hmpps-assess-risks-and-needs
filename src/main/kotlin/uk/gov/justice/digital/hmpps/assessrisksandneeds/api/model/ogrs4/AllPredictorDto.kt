package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ogrs4

import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.AllRisksPredictorAssessmentDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysNewAllPredictorDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysOspDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysRsrDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.RisksCrAssPredictorAssessmentDto
import java.math.BigDecimal

data class AllPredictorDto(
  val allReoffendingPredictor: StaticOrDynamicPredictorDto? = null,
  val violentReoffendingPredictor: StaticOrDynamicPredictorDto? = null,
  val seriousViolentReoffendingPredictor: StaticOrDynamicPredictorDto? = null,
  val directContactSexualReoffendingPredictor: BasePredictorDto? = null,
  val indirectImageContactSexualReoffendingPredictor: BasePredictorDto? = null,
  val combinedSeriousReoffendingPredictor: VersionedStaticOrDynamicPredictorDto? = null,
) {

  companion object {

    fun from(assessment: AllRisksPredictorAssessmentDto): AllPredictorDto = with(assessment) {
      buildAllPredictorDto(
        newAllPredictorScoresDto,
        ospScoreDto,
        rsrScoreDto,
      )
    }

    fun from(assessment: RisksCrAssPredictorAssessmentDto): AllPredictorDto = with(assessment) {
      buildAllPredictorDto(
        newAllPredictorScoresDto,
        ospScoreDto,
        rsrScoreDto,
      )
    }

    fun buildAllPredictorDto(
      newAllPredictorScoresDto: OasysNewAllPredictorDto?,
      osp: OasysOspDto,
      rsr: OasysRsrDto,
    ): AllPredictorDto = AllPredictorDto(
      allReoffendingPredictor = buildStaticOrDynamicPredictorDto(
        newAllPredictorScoresDto?.ogp2Calculated,
        newAllPredictorScoresDto?.ogp2Yr2,
        newAllPredictorScoresDto?.ogp2Band,
        newAllPredictorScoresDto?.ogrs4gCalculated,
        newAllPredictorScoresDto?.ogrs4gYr2,
        newAllPredictorScoresDto?.ogrs4gBand,
      ),

      violentReoffendingPredictor = buildStaticOrDynamicPredictorDto(
        newAllPredictorScoresDto?.ovp2Calculated,
        newAllPredictorScoresDto?.ovp2Yr2,
        newAllPredictorScoresDto?.ovp2Band,
        newAllPredictorScoresDto?.ogrs4vCalculated,
        newAllPredictorScoresDto?.ogrs4vYr2,
        newAllPredictorScoresDto?.ogrs4vBand,
      ),

      seriousViolentReoffendingPredictor = buildStaticOrDynamicPredictorDto(
        newAllPredictorScoresDto?.snsvDynamicCalculated,
        newAllPredictorScoresDto?.snsvDynamicYr2,
        newAllPredictorScoresDto?.snsvDynamicYr2Band,
        newAllPredictorScoresDto?.snsvStaticCalculated,
        newAllPredictorScoresDto?.snsvStaticYr2,
        newAllPredictorScoresDto?.snsvStaticYr2Band,
      ),

      directContactSexualReoffendingPredictor = BasePredictorDto(
        score = osp.ospDirectContactPercentageScore,
        band = ScoreLevel.findByType(osp.ospDirectContactScoreLevel),
      ),

      indirectImageContactSexualReoffendingPredictor = BasePredictorDto(
        score = osp.ospIndirectImagesChildrenPercentageScore,
        band = ScoreLevel.findByType(osp.ospIndirectImagesChildrenScoreLevel),
      ),

      combinedSeriousReoffendingPredictor = VersionedStaticOrDynamicPredictorDto(
        algorithmVersion = rsr.rsrAlgorithmVersion,
        staticOrDynamic = rsr.rsrStaticOrDynamic,
        score = rsr.rsrPercentageScore,
        band = ScoreLevel.findByType(rsr.scoreLevel),
      ),
    )

    fun buildStaticOrDynamicPredictorDto(
      dynamicCalculated: String?,
      dynamicYr2: BigDecimal?,
      dynamicBand: String?,
      staticCalculated: String?,
      staticYr2: BigDecimal?,
      staticBand: String?,
    ): StaticOrDynamicPredictorDto? = if (dynamicCalculated?.lowercase() == "y") {
      StaticOrDynamicPredictorDto(
        staticOrDynamic = ScoreType.DYNAMIC,
        score = dynamicYr2,
        band = ScoreLevel.findByType(dynamicBand),
      )
    } else if (staticCalculated?.lowercase() == "y") {
      StaticOrDynamicPredictorDto(
        staticOrDynamic = ScoreType.STATIC,
        score = staticYr2,
        band = ScoreLevel.findByType(staticBand),
      )
    } else {
      StaticOrDynamicPredictorDto()
    }
  }
}
