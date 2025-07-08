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
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentNeedsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskManagementPlansDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskScoresDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.View
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.AssessmentNeedsService
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.RiskManagementPlanService
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.RiskPredictorService
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.RiskService

@RestController
class IntegrationController(
  private val riskPredictorService: RiskPredictorService,
  private val riskService: RiskService,
  private val needsService: AssessmentNeedsService,
  private val riskManagementPlanService: RiskManagementPlanService,
) {
  @RequestMapping(path = ["/risks/predictors/{crn}"], method = [RequestMethod.GET])
  @Operation(description = "Gets risk predictors scores for all latest completed assessments from the last 1 year")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "403",
        description = "User does not have permission to access offender with provided CRN",
      ),
      ApiResponse(responseCode = "404", description = "Risk data does not exist for CRN"),
      ApiResponse(responseCode = "404", description = "Offender does not exist in Delius for provided CRN"),
      ApiResponse(responseCode = "404", description = "User does not exist in Delius for provided user name"),
      ApiResponse(responseCode = "401", description = "Unauthorised"),
      ApiResponse(responseCode = "200", description = "OK"),
    ],
  )
  @PreAuthorize("hasRole('ROLE_ARNS__RISKS__RO')")
  fun getAllRiskScores(@PathVariable crn: String): List<RiskScoresDto> = riskPredictorService.getAllRiskScoresWithoutLaoCheck(crn)

  @RequestMapping(path = ["/risks/rosh/{crn}"], method = [RequestMethod.GET])
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
  @PreAuthorize("hasRole('ROLE_ARNS__RISKS__RO')")
  @JsonView(View.AllRisksView::class)
  fun getRoshRisksByCrn(
    @Parameter(description = "CRN", required = true, example = "D1974X")
    @PathVariable
    crn: String,
  ): AllRoshRiskDto = riskService.getRoshRisksWithoutLaoCheck(crn)

  @RequestMapping(path = ["/risks/rosh/{crn}/{timeframe}"], method = [RequestMethod.GET])
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
  @PreAuthorize("hasRole('ROLE_ARNS__RISKS__RO')")
  @JsonView(View.AllRisksView::class)
  fun getRoshRisksByCrnWithinTimeframe(
    @Parameter(description = "CRN", required = true, example = "D1974X")
    @Parameter(description = "Timeframe", required = true, example = "70")
    @PathVariable crn: String,
    @PathVariable timeframe: Long,
  ): AllRoshRiskDto = riskService.getRoshRisksWithoutLaoCheck(crn, timeframe)

  @RequestMapping(path = ["/needs/{crn}"], method = [RequestMethod.GET])
  @Operation(description = "Gets criminogenic needs for crn")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "403", description = "Unauthorized"),
      ApiResponse(responseCode = "404", description = "CRN Not Found"),
      ApiResponse(responseCode = "200", description = "OK"),
    ],
  )
  @PreAuthorize("hasRole('ROLE_ARNS__RISKS__RO')")
  fun getCriminogenicNeedsByCrn(
    @Parameter(description = "CRN", required = true, example = "D1974X")
    @PathVariable
    crn: String,
  ): AssessmentNeedsDto = needsService.getAssessmentNeeds(crn)

  @RequestMapping(path = ["/needs/{crn}/{timeframe}"], method = [RequestMethod.GET])
  @Operation(description = "Gets criminogenic needs for crn within specified timeframe, measured in weeks")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "403", description = "Unauthorized"),
      ApiResponse(responseCode = "404", description = "CRN Not Found"),
      ApiResponse(responseCode = "200", description = "OK"),
    ],
  )
  @PreAuthorize("hasRole('ROLE_ARNS__RISKS__RO')")
  fun getCriminogenicNeedsByCrnWithinTimeframe(
    @Parameter(description = "CRN", required = true, example = "D1974X")
    @Parameter(description = "Timeframe", required = true, example = "70")
    @PathVariable crn: String,
    @PathVariable timeframe: Long,
  ): AssessmentNeedsDto = needsService.getAssessmentNeeds(crn, timeframe)

  @RequestMapping(path = ["/risks/risk-management-plan/{crn}"], method = [RequestMethod.GET])
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
  @PreAuthorize("hasRole('ROLE_ARNS__RISKS__RO')")
  fun getRiskManagementPlan(
    @Parameter(description = "CRN", required = true, example = "D1974X")
    @PathVariable
    crn: String,
  ): RiskManagementPlansDto = riskManagementPlanService.getRiskManagementPlanWithoutLaoCheck(crn)
}
