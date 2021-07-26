package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OffenderAndOffencesDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PredictorType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskPredictorsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.AssessmentApiRestClient

@Service
class RiskPredictorService(private val assessmentClient: AssessmentApiRestClient) {
  fun getPredictorScores(predictorType: PredictorType, offenderAndOffences: OffenderAndOffencesDto): RiskPredictorsDto? {
    return assessmentClient.calculatePredictorTypeScoring(predictorType, offenderAndOffences)
  }
}
