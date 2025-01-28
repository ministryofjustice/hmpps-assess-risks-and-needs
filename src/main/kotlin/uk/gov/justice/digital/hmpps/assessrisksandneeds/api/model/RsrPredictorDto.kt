package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

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
  val ospiiPercentageScore: BigDecimal? = null,
  val ospdcPercentageScore: BigDecimal? = null,
  val ospiiScoreLevel: ScoreLevel? = null,
  val ospdcScoreLevel: ScoreLevel? = null,
  val calculatedDate: LocalDateTime? = null,
  val completedDate: LocalDateTime? = null,
  val signedDate: LocalDateTime? = null,
  val staticOrDynamic: ScoreType? = null,
  val source: RsrScoreSource,
  val status: AssessmentStatus,
  val algorithmVersion: String? = null,
) {

  companion object {

    fun from(oasysPredictorsDtos: List<RiskPredictorAssessmentDto>): List<RsrPredictorDto> = oasysPredictorsDtos.map { from(it) }

    fun from(oasysPredictorsDto: RiskPredictorAssessmentDto): RsrPredictorDto {
      with(oasysPredictorsDto) {
        return RsrPredictorDto(
          rsrPercentageScore = rsrScoreDto.rsrPercentageScore,
          rsrScoreLevel = ScoreLevel.findByType(rsrScoreDto.scoreLevel),
          ospcPercentageScore = ospScoreDto.ospContactPercentageScore,
          ospcScoreLevel = ospScoreDto.ospContactScoreLevel?.let { ScoreLevel.findByType(it) },
          ospiPercentageScore = ospScoreDto.ospImagePercentageScore,
          ospiScoreLevel = ospScoreDto.ospImageScoreLevel?.let { ScoreLevel.findByType(it) },
          ospiiPercentageScore = ospScoreDto.ospIndirectImagesChildrenPercentageScore,
          ospdcPercentageScore = ospScoreDto.ospDirectContactPercentageScore,
          ospiiScoreLevel = ospScoreDto.ospIndirectImagesChildrenScoreLevel?.let { ScoreLevel.findByType(it) },
          ospdcScoreLevel = ospScoreDto.ospDirectContactScoreLevel?.let { ScoreLevel.findByType(it) },
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
  }
}

enum class RsrScoreSource {
  OASYS,
}
