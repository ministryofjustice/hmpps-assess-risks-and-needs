package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentNeedsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentOffenceDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskManagementPlanORDSDetailsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.AssessmentNeedsService
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.AssessmentOffenceService
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.RiskManagementPlanService

@RestController
class AssessmentController(
  private val assessmentNeedsService: AssessmentNeedsService,
  private val assessmentOffenceService: AssessmentOffenceService,
  private val riskManagementPlanService: RiskManagementPlanService
) {

  @RequestMapping(path = ["/needs/crn/{crn}"], method = [RequestMethod.GET])
  @Operation(description = "Gets criminogenic needs for crn")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "403", description = "Unauthorized"),
      ApiResponse(responseCode = "404", description = "CRN Not Found"),
      ApiResponse(responseCode = "200", description = "OK")
    ]
  )

  @PreAuthorize("hasAnyRole('ROLE_PROBATION')")
  fun getCriminogenicNeedsByCrn(
    @Parameter(description = "CRN", required = true, example = "D1974X")
    @PathVariable crn: String,
  ): AssessmentNeedsDto {
    return assessmentNeedsService.getAssessmentNeeds(crn)
  }

  @RequestMapping(path = ["/assessments/crn/{crn}/offence"], method = [RequestMethod.GET])
  @Operation(description = "Gets offence details from latest complete assessments for crn")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "403", description = "Unauthorized"),
      ApiResponse(responseCode = "404", description = "CRN Not Found"),
      ApiResponse(responseCode = "200", description = "OK")
    ]
  )
  @PreAuthorize("hasAnyRole('ROLE_PROBATION')")
  fun getAssessmentOffenceDetails(
    @Parameter(description = "CRN", required = true, example = "D1974X")
    @PathVariable crn: String,
  ): AssessmentOffenceDto {
    return assessmentOffenceService.getAssessmentOffence(crn)
  }

  @RequestMapping(path = ["/assessments/crn/{crn}/riskManagementPlan"], method = [RequestMethod.GET])
  @Operation(description = "Gets Risk Management Plan from latest complete assessments for crn")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "403", description = "Unauthorized"),
      ApiResponse(responseCode = "404", description = "CRN Not Found"),
      ApiResponse(responseCode = "200", description = "OK")
    ]
  )
  @PreAuthorize("hasAnyRole('ROLE_PROBATION')")
  fun getRiskManagementPlan(
    @Parameter(description = "CRN", required = true, example = "D1974X")
    @PathVariable crn: String,
  ): RiskManagementPlanORDSDetailsDto {
    return riskManagementPlanService.getRiskManagementPlans(crn)
  }
}
