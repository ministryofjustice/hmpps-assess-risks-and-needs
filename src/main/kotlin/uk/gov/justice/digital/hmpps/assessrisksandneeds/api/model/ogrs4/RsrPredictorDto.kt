package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ogrs4

import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.AllRisksPredictorAssessmentDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysNewAllPredictorDto

data class RsrPredictorDto(
  val seriousViolentReoffendingPredictor: StaticOrDynamicPredictorDto? = null,
  val directContactSexualReoffendingPredictor: BasePredictorDto? = null,
  val indirectImageContactSexualReoffendingPredictor: BasePredictorDto? = null,
  val combinedSeriousReoffendingPredictor: VersionedStaticOrDynamicPredictorDto? = null,
) {

  companion object {

    fun from(oasysPredictorsDtos: List<AllRisksPredictorAssessmentDto>): List<RsrPredictorDto> = oasysPredictorsDtos.map { from(it) }

    fun from(oasysPredictorsDto: AllRisksPredictorAssessmentDto): RsrPredictorDto {
      with(oasysPredictorsDto) {
        return RsrPredictorDto(
          seriousViolentReoffendingPredictor = populateSeriousViolentPredictor(oasysPredictorsDto.newAllPredictorScoresDto),
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

    fun populateSeriousViolentPredictor(dto: OasysNewAllPredictorDto?): StaticOrDynamicPredictorDto? = if (dto?.snsvDynamicCalculated?.lowercase() == "y") {
      StaticOrDynamicPredictorDto(
        staticOrDynamic = ScoreType.DYNAMIC,
        score = dto.snsvDynamicYr2,
        band = ScoreLevel.findByType(dto.snsvDynamicYr2Band),
      )
    } else if (dto?.snsvStaticCalculated?.lowercase() == "y") {
      StaticOrDynamicPredictorDto(
        staticOrDynamic = ScoreType.STATIC,
        score = dto.snsvStaticYr2,
        band = ScoreLevel.findByType(dto.snsvStaticYr2Band),
      )
    } else {
      StaticOrDynamicPredictorDto()
    }
  }
}
