package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ogrs4

import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.AllRisksPredictorAssessmentDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysOgp2Dto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysOgrs4gDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysOgrs4vDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysOspDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysOvp2Dto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysRsrDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysSnsvDto
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
        ogp2ScoreDto,
        ogrs4gScoreDto,
        ovp2ScoreDto,
        ogrs4vScoreDto,
        snsvScoreDto,
        ospScoreDto,
        rsrScoreDto,
      )
    }

    fun from(assessment: RisksCrAssPredictorAssessmentDto): AllPredictorDto = with(assessment) {
      buildAllPredictorDto(
        ogp2ScoreDto,
        ogrs4gScoreDto,
        ovp2ScoreDto,
        ogrs4vScoreDto,
        snsvScoreDto,
        ospScoreDto,
        rsrScoreDto,
      )
    }

    fun buildAllPredictorDto(
      ogp: OasysOgp2Dto?,
      ogrs4g: OasysOgrs4gDto?,
      ovp: OasysOvp2Dto?,
      ogrs4v: OasysOgrs4vDto?,
      snsv: OasysSnsvDto?,
      osp: OasysOspDto,
      rsr: OasysRsrDto,
    ): AllPredictorDto = AllPredictorDto(
      allReoffendingPredictor = buildStaticOrDynamicPredictorDto(
        ogp?.ogp2Calculated,
        ogp?.ogp2Yr2,
        ogp?.ogp2Band,
        ogrs4g?.ogrs4gCalculated,
        ogrs4g?.ogrs4gYr2,
        ogrs4g?.ogrs4gBand,
      ),

      violentReoffendingPredictor = buildStaticOrDynamicPredictorDto(
        ovp?.ovp2Calculated,
        ovp?.ovp2Yr2,
        ovp?.ovp2Band,
        ogrs4v?.ogrs4vCalculated,
        ogrs4v?.ogrs4vYr2,
        ogrs4v?.ogrs4vBand,
      ),

      seriousViolentReoffendingPredictor = buildStaticOrDynamicPredictorDto(
        snsv?.snsvDynamicCalculated,
        snsv?.snsvDynamicYr2,
        snsv?.snsvDynamicYr2Band,
        snsv?.snsvStaticCalculated,
        snsv?.snsvStaticYr2,
        snsv?.snsvStaticYr2Band,
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
