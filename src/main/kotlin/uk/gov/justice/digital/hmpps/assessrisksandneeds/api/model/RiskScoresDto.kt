package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import java.time.LocalDateTime

class RiskScoresDto(
  val completedDate: LocalDateTime? = null,
  val ogr: OgrScoreDto? = null,
  val ovp: OvpScoreDto? = null,
  val ogp: OgpScoreDto? = null,
  val rsr: RsrScoreDto? = null,
  val osp: OspScoreDto? = null

) {

}
