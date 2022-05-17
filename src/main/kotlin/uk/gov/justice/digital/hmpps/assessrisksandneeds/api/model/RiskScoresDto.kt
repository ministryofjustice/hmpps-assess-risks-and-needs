package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysPredictorsDto
import java.time.LocalDateTime

class RiskScoresDto(
  val completedDate: LocalDateTime? = null,
  val ogr: OgrScoreDto? = null,
  val ovp: OvpScoreDto? = null,
  val ogp: OgpScoreDto? = null,
  val rsr: RsrPredictorDto? = null,
) {

  companion object{
    fun from(allRisks: List<OasysPredictorsDto>?) {}

    //TODO
  }

}
