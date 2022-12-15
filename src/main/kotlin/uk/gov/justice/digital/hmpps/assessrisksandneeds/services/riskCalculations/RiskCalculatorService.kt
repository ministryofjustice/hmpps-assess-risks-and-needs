package uk.gov.justice.digital.hmpps.assessrisksandneeds.services.riskCalculations

import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OffenderAndOffencesDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PredictorType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskPredictorsDto

interface RiskCalculatorService {
  fun calculatePredictorScores(
    predictorType: PredictorType,
    offenderAndOffences: OffenderAndOffencesDto,
    algorithmVersion: String? = null
  ): RiskPredictorsDto
}
