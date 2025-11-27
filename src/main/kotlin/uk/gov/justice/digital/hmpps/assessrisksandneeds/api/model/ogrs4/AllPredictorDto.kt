package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ogrs4

import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.RiskPredictorAssessmentDto

data class AllPredictorDto(
  val allReoffendingPredictor: StaticOrDynamicPredictorDto? = null,
  val violentReoffendingPredictor: StaticOrDynamicPredictorDto? = null,
  val seriousViolentReoffendingPredictor: StaticOrDynamicPredictorDto? = null,
  val directContactSexualReoffendingPredictor: BasePredictorDto? = null,
  val indirectImageContactSexualReoffendingPredictor: BasePredictorDto? = null,
  val combinedSeriousReoffendingPredictor: VersionedStaticOrDynamicPredictorDto? = null,
) {

  companion object {

    fun from(assessment: RiskPredictorAssessmentDto): AllPredictorDto = AllPredictorDto(
      allReoffendingPredictor = populateAllPredictor(assessment),
      violentReoffendingPredictor = populateViolentPredictor(assessment),
      seriousViolentReoffendingPredictor = populateSeriousViolentPredictor(assessment),
      directContactSexualReoffendingPredictor = BasePredictorDto(
        score = assessment.ospScoreDto.ospDirectContactPercentageScore,
        band = ScoreLevel.findByType(assessment.ospScoreDto.ospDirectContactScoreLevel),
      ),
      indirectImageContactSexualReoffendingPredictor = BasePredictorDto(
        score = assessment.ospScoreDto.ospIndirectImagesChildrenPercentageScore,
        band = ScoreLevel.findByType(assessment.ospScoreDto.ospIndirectImagesChildrenScoreLevel),
      ),
      combinedSeriousReoffendingPredictor = VersionedStaticOrDynamicPredictorDto(
        algorithmVersion = assessment.rsrScoreDto.rsrAlgorithmVersion,
        staticOrDynamic = assessment.rsrScoreDto.rsrStaticOrDynamic,
        score = assessment.rsrScoreDto.rsrPercentageScore,
        band = ScoreLevel.findByType(assessment.rsrScoreDto.scoreLevel),
      ),
    )

    fun populateAllPredictor(dto: RiskPredictorAssessmentDto): StaticOrDynamicPredictorDto = if (dto.ogp2ScoreDto?.ogp2Calculated?.lowercase() == "y") {
      StaticOrDynamicPredictorDto(
        staticOrDynamic = ScoreType.DYNAMIC,
        score = dto.ogp2ScoreDto.ogp2Yr2,
        band = ScoreLevel.findByType(dto.ogp2ScoreDto.ogp2Band),
      )
    } else if (dto.ogrs4gScoreDto?.ogrs4gCalculated?.lowercase() == "y") {
      StaticOrDynamicPredictorDto(
        staticOrDynamic = ScoreType.STATIC,
        score = dto.ogrs4gScoreDto.ogrs4gYr2,
        band = ScoreLevel.findByType(dto.ogrs4gScoreDto.ogrs4gBand),
      )
    } else {
      StaticOrDynamicPredictorDto()
    }

    fun populateViolentPredictor(dto: RiskPredictorAssessmentDto): StaticOrDynamicPredictorDto? = if (dto.ovp2ScoreDto?.ovp2Calculated?.lowercase() == "y") {
      StaticOrDynamicPredictorDto(
        staticOrDynamic = ScoreType.DYNAMIC,
        score = dto.ovp2ScoreDto.ovp2Yr2,
        band = ScoreLevel.findByType(dto.ovp2ScoreDto.ovp2Band),
      )
    } else if (dto.ogrs4vScoreDto?.ogrs4vCalculated?.lowercase() == "y") {
      StaticOrDynamicPredictorDto(
        staticOrDynamic = ScoreType.STATIC,
        score = dto.ogrs4vScoreDto.ogrs4vYr2,
        band = ScoreLevel.findByType(dto.ogrs4vScoreDto.ogrs4vBand),
      )
    } else {
      StaticOrDynamicPredictorDto()
    }

    fun populateSeriousViolentPredictor(dto: RiskPredictorAssessmentDto): StaticOrDynamicPredictorDto? = if (dto.snsvScoreDto?.snsvDynamicCalculated?.lowercase() == "y") {
      StaticOrDynamicPredictorDto(
        staticOrDynamic = ScoreType.DYNAMIC,
        score = dto.snsvScoreDto.snsvDynamicYr2,
        band = ScoreLevel.findByType(dto.snsvScoreDto.snsvDynamicYr2Band),
      )
    } else if (dto.snsvScoreDto?.snsvStaticCalculated?.lowercase() == "y") {
      StaticOrDynamicPredictorDto(
        staticOrDynamic = ScoreType.STATIC,
        score = dto.snsvScoreDto.snsvStaticYr2,
        band = ScoreLevel.findByType(dto.snsvScoreDto.snsvStaticYr2Band),
      )
    } else {
      StaticOrDynamicPredictorDto()
    }
  }
}
