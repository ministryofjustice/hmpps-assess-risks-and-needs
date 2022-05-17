package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import java.math.BigDecimal
import java.time.LocalDateTime

class RsrScoreDto(
  val rsrPercentageScore: BigDecimal? = null,
  val rsrScoreLevel: ScoreLevel? = null,
  val ospcPercentageScore: BigDecimal? = null,
  val ospcScoreLevel: ScoreLevel? = null,
  val ospiPercentageScore: BigDecimal? = null,
  val ospiScoreLevel: ScoreLevel? = null,
  val calculatedDate: LocalDateTime? = null,
  val completedDate: LocalDateTime? = null,
  val signedDate: LocalDateTime? = null,
  val staticOrDynamic: ScoreType? = null,
  val source: RsrScoreSource,
  val status: AssessmentStatus,
  val algorithmVersion: String? = null,
) {

}
