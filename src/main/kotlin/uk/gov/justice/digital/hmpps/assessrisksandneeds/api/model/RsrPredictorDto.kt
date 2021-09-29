package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysPredictorsDto
import java.math.BigDecimal
import java.time.LocalDateTime

data class RsrPredictorDto(
  val percentageScore: BigDecimal? = null,
  val scoreLevel: ScoreLevel? = null,
  val calculatedDate: LocalDateTime? = null,
  val completedDate: LocalDateTime? = null,
  val signedDate: LocalDateTime? = null,
  val staticOrDynamic: ScoreType? = null,
  val source: PredictorSource,
  val status: AssessmentStatus,
  val algorithmVersion: String? = null,
) {
  // TODO: 29/09/2021 check for variable types and nulls
  companion object{
    fun from(oasysPredictorsDto: OasysPredictorsDto): RsrPredictorDto? {
      // TODO: 29/09/2021 check possible assessment statuses 
      if (AssessmentStatus.findBy(oasysPredictorsDto.assessmentStatus!!) == null) {
        return null
      } else {
        with(oasysPredictorsDto.rsr!!) {
          return RsrPredictorDto(
            percentageScore = rsrPercentageScore,
            scoreLevel = ScoreLevel.findByType(rsrRiskRecon?.code!!),
            calculatedDate = null,
            completedDate = oasysPredictorsDto.completedDate,
            signedDate = null,
            staticOrDynamic = ScoreType.findByType(rsrStaticOrDynamic!!),
            source = PredictorSource.OASYS,
            status = AssessmentStatus.findBy(oasysPredictorsDto.assessmentStatus!!)!!,
            algorithmVersion = rsrAlgorithmVersion.toString()
          )
        }
      }
    }
  }
}

enum class AssessmentStatus(val status: String) {
  COMPLETED("Completed"), LOCKED_INCOMPLETE("Locked Incomplete");

  companion object {
    fun findBy(status: String): AssessmentStatus? {
      return values().firstOrNull { value -> value.status == status }
    }
  }
}

