package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OffenderAndOffencesDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PredictorType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskPredictorsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.RiskPredictorService

@RestController
class RiskPredictorsController(private val riskPredictorService: RiskPredictorService) {
  @RequestMapping(path = ["/risks/predictors/{predictorType}"], method = [RequestMethod.POST])
  @Operation(description = "Gets risk predictors for a specific predictor type")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "403", description = "Unauthorized"),
      ApiResponse(responseCode = "200", description = "OK")
    ]
  )
  @PreAuthorize("hasAnyRole('ROLE_PROBATION')")
  fun getRiskPredictorsByPredictorType(
    @Parameter(description = "Predictor type", required = true, example = "RSR")
    @PathVariable predictorType: PredictorType,
    @RequestParam(value = "final", required = true) final: Boolean,
    @RequestParam(value = "source", required = true) source: String,
    @RequestParam(value = "sourceId", required = true) sourceId: String,
    @RequestBody offenderAndOffences: OffenderAndOffencesDto
  ): RiskPredictorsDto {
    return riskPredictorService.calculatePredictorScores(predictorType, offenderAndOffences, final, source, sourceId)
  }
}
