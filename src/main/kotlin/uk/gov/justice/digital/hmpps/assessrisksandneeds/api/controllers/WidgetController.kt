package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.controllers

import com.fasterxml.jackson.annotation.JsonView
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RoshRiskWidgetDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.View
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.RiskService

@RestController
class WidgetController(
  private val riskService: RiskService,
) {

  @RequestMapping(path = ["/risks/crn/{crn}/widget"], method = [RequestMethod.GET])
  @Operation(description = "Gets rosh summary for crn")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "403", description = "Unauthorized"),
      ApiResponse(responseCode = "404", description = "CRN Not Found"),
      ApiResponse(responseCode = "200", description = "OK"),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_PROBATION', 'ROLE_CRS_PROVIDER')")
  fun getRiskSummaryByCrn(
    @Parameter(description = "CRN", required = true, example = "D1974X")
    @JsonView(View.SingleRisksView::class)
    @PathVariable
    crn: String,
  ): RoshRiskWidgetDto = riskService.getRoshRiskWidgetDataForCrn(crn)

  @RequestMapping(path = ["/risks/crn/{crn}/widget/{timeframe}"], method = [RequestMethod.GET])
  @Operation(description = "Gets rosh summary for crn within specified timeframe, measured in weeks")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "403", description = "Unauthorized"),
      ApiResponse(responseCode = "404", description = "CRN Not Found"),
      ApiResponse(responseCode = "200", description = "OK"),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_PROBATION', 'ROLE_CRS_PROVIDER')")
  fun getRiskSummaryByCrnWithinTimeframe(
    @Parameter(description = "CRN", required = true, example = "D1974X")
    @Parameter(description = "Timeframe", required = true, example = "70")
    @JsonView(View.SingleRisksView::class)
    @PathVariable crn: String,
    @PathVariable timeframe: Long,
  ): RoshRiskWidgetDto = riskService.getRoshRiskWidgetDataForCrn(crn, timeframe)
}
