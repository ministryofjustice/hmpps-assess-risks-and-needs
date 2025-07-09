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
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AllRoshRiskDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskManagementPlansDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskRoshSummaryDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.View
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.RiskManagementPlanService
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.RiskService

@RestController
class RisksController(
  private val riskService: RiskService,
  private val riskManagementPlanService: RiskManagementPlanService,
) {

  @RequestMapping(path = ["/risks/crn/{crn}/summary"], method = [RequestMethod.GET])
  @Operation(description = "Gets rosh summary for crn. Returns only assessments completed within the last year.")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "403", description = "Unauthorized"),
      ApiResponse(responseCode = "404", description = "CRN Not Found"),
      ApiResponse(responseCode = "200", description = "OK"),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_PROBATION', 'ROLE_CRS_PROVIDER', 'ROLE_ACCREDITED_PROGRAMS_RO', 'ROLE_OFFENDER_CATEGORISATION_RO')")
  fun getRiskSummaryByCrn(
    @Parameter(description = "CRN", required = true, example = "D1974X")
    @JsonView(View.SingleRisksView::class)
    @PathVariable
    crn: String,
  ): RiskRoshSummaryDto = riskService.getRoshRiskSummaryByCrn(crn)

  @RequestMapping(path = ["/risks/crn/{crn}/summary/{timeframe}"], method = [RequestMethod.GET])
  @Operation(description = "Gets rosh summary for crn. Returns only assessments completed within specified timeframe, measured in weeks.")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "403", description = "Unauthorized"),
      ApiResponse(responseCode = "404", description = "CRN Not Found"),
      ApiResponse(responseCode = "200", description = "OK"),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_PROBATION', 'ROLE_CRS_PROVIDER', 'ROLE_ACCREDITED_PROGRAMS_RO', 'ROLE_OFFENDER_CATEGORISATION_RO')")
  fun getRiskSummaryByCrnWithinTimeframe(
    @Parameter(description = "CRN", required = true, example = "D1974X")
    @Parameter(description = "Timeframe", required = true, example = "70")
    @JsonView(View.SingleRisksView::class)
    @PathVariable crn: String,
    @PathVariable timeframe: Long,
  ): RiskRoshSummaryDto = riskService.getRoshRiskSummaryByCrn(crn, timeframe)

  @RequestMapping(path = ["/risks/crn/{crn}"], method = [RequestMethod.GET])
  @Operation(
    description = "Gets ROSH risks for crn. Only returns freeform text concerns for risk to self where answer to corresponding risk question is Yes. " +
      "Returns only assessments completed within the last year",
  )
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "403", description = "Unauthorized"),
      ApiResponse(responseCode = "404", description = "CRN Not Found"),
      ApiResponse(responseCode = "200", description = "OK"),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_PROBATION', 'ROLE_CRS_PROVIDER', 'ROLE_OFFENDER_RISK_RO', 'ROLE_RISK_RESETTLEMENT_PASSPORT_RO', 'ROLE_RISK_INTEGRATIONS_RO')")
  @JsonView(View.AllRisksView::class)
  fun getRoshRisksByCrn(
    @Parameter(description = "CRN", required = true, example = "D1974X")
    @PathVariable
    crn: String,
  ): AllRoshRiskDto = riskService.getRoshRisksByCrn(crn)

  @RequestMapping(path = ["/risks/crn/{crn}/{timeframe}"], method = [RequestMethod.GET])
  @Operation(
    description = "Gets ROSH risks for crn. Only returns freeform text concerns for risk to self where answer to corresponding risk question is Yes. " +
      "Returns only assessments completed within specified timeframe, measured in weeks",
  )
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "403", description = "Unauthorized"),
      ApiResponse(responseCode = "404", description = "CRN Not Found"),
      ApiResponse(responseCode = "200", description = "OK"),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_PROBATION', 'ROLE_CRS_PROVIDER', 'ROLE_OFFENDER_RISK_RO', 'ROLE_RISK_RESETTLEMENT_PASSPORT_RO', 'ROLE_RISK_INTEGRATIONS_RO')")
  @JsonView(View.AllRisksView::class)
  fun getRoshRisksByCrnWithinTimeframe(
    @Parameter(description = "CRN", required = true, example = "D1974X")
    @Parameter(description = "Timeframe", required = true, example = "70")
    @PathVariable crn: String,
    @PathVariable timeframe: Long,
  ): AllRoshRiskDto = riskService.getRoshRisksByCrn(crn, timeframe)

  @RequestMapping(path = ["/risks/crn/{crn}/fulltext"], method = [RequestMethod.GET])
  @Operation(
    description = "Gets ROSH risks for crn. Returns freeform concerns text regardless of answer to corresponding risk question. " +
      "Returns only assessments completed within the last year",
  )
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "403", description = "Unauthorized"),
      ApiResponse(responseCode = "404", description = "CRN Not Found"),
      ApiResponse(responseCode = "200", description = "OK"),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_PROBATION', 'ROLE_CRS_PROVIDER')")
  @JsonView(View.AllRisksView::class)
  fun getFulltextRoshRisksByCrn(
    @Parameter(description = "CRN", required = true, example = "D1974X")
    @PathVariable
    crn: String,
  ): AllRoshRiskDto = riskService.getFulltextRoshRisksByCrn(crn)

  @RequestMapping(path = ["/risks/crn/{crn}/fulltext/{timeframe}"], method = [RequestMethod.GET])
  @Operation(
    description = "Gets ROSH risks for crn. Returns freeform concerns text regardless of answer to corresponding risk question. " +
      "Returns only assessments completed within specified timeframe, measured in weeks",
  )
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "403", description = "Unauthorized"),
      ApiResponse(responseCode = "404", description = "CRN Not Found"),
      ApiResponse(responseCode = "200", description = "OK"),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_PROBATION', 'ROLE_CRS_PROVIDER')")
  @JsonView(View.AllRisksView::class)
  fun getFulltextRoshRisksByCrnWithinTimeframe(
    @Parameter(description = "CRN", required = true, example = "D1974X")
    @Parameter(description = "Timeframe", required = true, example = "70")
    @PathVariable crn: String,
    @PathVariable timeframe: Long,
  ): AllRoshRiskDto = riskService.getFulltextRoshRisksByCrn(crn, timeframe)

  @RequestMapping(path = ["/risks/crn/{crn}/risk-management-plan"], method = [RequestMethod.GET])
  @Operation(description = "Gets Risk Management Plan from latest complete assessments for crn")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "403", description = "User does not have permission to access offender with provided CRN"),
      ApiResponse(responseCode = "404", description = "Risk management plan data does not exist for CRN"),
      ApiResponse(responseCode = "404", description = "Offender does not exist in Delius for provided CRN"),
      ApiResponse(responseCode = "404", description = "User does not exist in Delius for provided user name"),
      ApiResponse(responseCode = "401", description = "Unauthorised"),
      ApiResponse(responseCode = "200", description = "OK"),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_PROBATION')")
  fun getRiskManagementPlan(
    @Parameter(description = "CRN", required = true, example = "D1974X")
    @PathVariable
    crn: String,
  ): RiskManagementPlansDto = riskManagementPlanService.getRiskManagementPlans(crn)
}
