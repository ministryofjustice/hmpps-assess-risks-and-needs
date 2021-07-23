package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OffenderAndOffencesDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PredictorType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskPredictorsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.Score
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreLevel
import java.math.BigDecimal

@Service
class RiskPredictorService {
  fun getPredictorScores(predictorType: String, offenderAndOffences: OffenderAndOffencesDto): RiskPredictorsDto {
    return RiskPredictorsDto(rsrScore = Score(PredictorType.RSR, ScoreLevel.HIGH, BigDecimal("11.34")))
  }
}
