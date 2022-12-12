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
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OtherRoshRisksDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskManagementPlansDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskRoshSummaryDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RoshRiskToSelfDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.View
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.RiskManagementPlanService
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.RiskService

@RestController
class RisksController(
  private val riskService: RiskService,
  private val riskManagementPlanService: RiskManagementPlanService
) {

  @RequestMapping(path = ["/risks/crn/{crn}/summary"], method = [RequestMethod.GET])
  @Operation(description = "Gets rosh summary for crn. Returns only assessments completed withing the last year.")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "403", description = "Unauthorized"),
      ApiResponse(responseCode = "404", description = "CRN Not Found"),
      ApiResponse(responseCode = "200", description = "OK")
    ]
  )
  @PreAuthorize("hasAnyRole('ROLE_PROBATION', 'ROLE_CRS_PROVIDER')")
  fun getRiskSummaryByCrn(
    @Parameter(description = "CRN", required = true, example = "D1974X")
    @JsonView(View.SingleRisksView::class)
    @PathVariable crn: String
  ): RiskRoshSummaryDto {
    return riskService.getRoshRiskSummaryByCrn(crn)
  }

  @RequestMapping(path = ["/risks/crn/{crn}/self"], method = [RequestMethod.GET])
  @Operation(
    description = "Gets ROSH to individual for crn. Only returns freeform text where answer to " +
      "corresponding risk question is Yes. Returns only assessments completed withing the last year"
  )
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "403", description = "Unauthorized"),
      ApiResponse(responseCode = "404", description = "CRN Not Found"),
      ApiResponse(responseCode = "200", description = "OK")
    ]
  )
  @PreAuthorize("hasAnyRole('ROLE_PROBATION', 'ROLE_CRS_PROVIDER')")
  fun getRiskToSelfByCrn(
    @Parameter(description = "CRN", required = true, example = "D1974X")
    @JsonView(View.SingleRisksView::class)
    @PathVariable crn: String
  ): RoshRiskToSelfDto {
    return riskService.getRoshRisksToSelfByCrn(crn)
  }

  @RequestMapping(path = ["/risks/crn/{crn}/other"], method = [RequestMethod.GET])
  @Operation(description = "Gets 'other' ROSH risks for crn. Returns only assessments completed withing the last year")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "403", description = "Unauthorized"),
      ApiResponse(responseCode = "404", description = "CRN Not Found"),
      ApiResponse(responseCode = "200", description = "OK")
    ]
  )
  @PreAuthorize("hasAnyRole('ROLE_PROBATION', 'ROLE_CRS_PROVIDER')")
  fun getOtherRisksByCrn(
    @Parameter(description = "CRN", required = true, example = "D1974X")
    @JsonView(View.SingleRisksView::class)
    @PathVariable crn: String,
  ): OtherRoshRisksDto {
    return riskService.getOtherRoshRisk(crn)
  }

  @RequestMapping(path = ["/risks/crn/{crn}"], method = [RequestMethod.GET])
  @Operation(
    description = "Gets ROSH risks for crn. Only returns freeform text concerns for risk to self where answer to corresponding risk question is Yes." +
      "Returns only assessments completed withing the last year"
  )
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "403", description = "Unauthorized"),
      ApiResponse(responseCode = "404", description = "CRN Not Found"),
      ApiResponse(responseCode = "200", description = "OK")
    ]
  )
  @PreAuthorize("hasAnyRole('ROLE_PROBATION', 'ROLE_CRS_PROVIDER')")
  @JsonView(View.AllRisksView::class)
  fun getRoshRisksByCrn(
    @Parameter(description = "CRN", required = true, example = "D1974X")
    @PathVariable crn: String,
  ): AllRoshRiskDto {
    return riskService.getRoshRisksByCrn(crn)
  }

  @RequestMapping(path = ["/risks/crn/{crn}/fulltext"], method = [RequestMethod.GET])
  @Operation(
    description = "Gets ROSH risks for crn. Returns freeform corncerns text regardless of answer to corresponding risk question. " +
      "Returns only assessments completed withing the last year"
  )
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "403", description = "Unauthorized"),
      ApiResponse(responseCode = "404", description = "CRN Not Found"),
      ApiResponse(responseCode = "200", description = "OK")
    ]
  )
  @PreAuthorize("hasAnyRole('ROLE_PROBATION', 'ROLE_CRS_PROVIDER')")
  @JsonView(View.AllRisksView::class)
  fun getFulltextRoshRisksByCrn(
    @Parameter(description = "CRN", required = true, example = "D1974X")
    @PathVariable crn: String,
  ): AllRoshRiskDto {
    return riskService.getFulltextRoshRisksByCrn(crn)
  }

  @RequestMapping(path = ["/risks/crn/{crn}/risk-management-plan"], method = [RequestMethod.GET])
  @Operation(description = "Gets Risk Management Plan from latest complete assessments for crn")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "403", description = "User does not have permission to access offender with provided CRN"),
      ApiResponse(responseCode = "404", description = "Risk management plan data does not exist for CRN"),
      ApiResponse(responseCode = "404", description = "Offender does not exist in Delius for provided CRN"),
      ApiResponse(responseCode = "404", description = "User does not exist in Delius for provided user name"),
      ApiResponse(responseCode = "401", description = "Unauthorised"),
      ApiResponse(responseCode = "200", description = "OK")
    ]
  )
  @PreAuthorize("hasAnyRole('ROLE_PROBATION')")
  fun getRiskManagementPlan(
    @Parameter(description = "CRN", required = true, example = "D1974X")
    @PathVariable crn: String,
  ): RiskManagementPlansDto {
    return riskManagementPlanService.getRiskManagementPlans(crn)
  }
}
