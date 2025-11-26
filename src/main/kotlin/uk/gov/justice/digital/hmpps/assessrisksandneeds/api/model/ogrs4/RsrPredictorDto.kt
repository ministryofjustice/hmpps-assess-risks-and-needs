package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ogrs4

import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.RiskPredictorAssessmentDto

data class RsrPredictorDto(
  val seriousViolentReoffendingPredictor: StaticOrDynamicPredictorDto? = null,
  val directContactSexualReoffendingPredictor: BasePredictorDto? = null,
  val indirectImageContactSexualReoffendingPredictor: BasePredictorDto? = null,
  val combinedSeriousReoffendingPredictor: VersionedStaticOrDynamicPredictorDto? = null,
) {

  companion object {

    fun from(oasysPredictorsDtos: List<RiskPredictorAssessmentDto>): List<RsrPredictorDto> = oasysPredictorsDtos.map { from(it) }

    fun from(oasysPredictorsDto: RiskPredictorAssessmentDto): RsrPredictorDto {
      with(oasysPredictorsDto) {
        return RsrPredictorDto(
          seriousViolentReoffendingPredictor = populateSeriousViolentPredictor(oasysPredictorsDto),
          directContactSexualReoffendingPredictor = BasePredictorDto(
            score = ospScoreDto.ospDirectContactPercentageScore,
            band = ScoreLevel.findByType(ospScoreDto.ospDirectContactScoreLevel),
          ),
          indirectImageContactSexualReoffendingPredictor = BasePredictorDto(
            score = ospScoreDto.ospIndirectImagesChildrenPercentageScore,
            band = ScoreLevel.findByType(ospScoreDto.ospIndirectImagesChildrenScoreLevel),
          ),
          combinedSeriousReoffendingPredictor = VersionedStaticOrDynamicPredictorDto(
            algorithmVersion = rsrScoreDto.rsrAlgorithmVersion,
            staticOrDynamic = rsrScoreDto.rsrStaticOrDynamic,
            score = rsrScoreDto.rsrPercentageScore,
            band = ScoreLevel.findByType(rsrScoreDto.scoreLevel),
          ),
        )
      }
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
