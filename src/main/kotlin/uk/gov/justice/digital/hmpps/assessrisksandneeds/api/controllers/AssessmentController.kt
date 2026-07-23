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
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PersonIdentifier
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.MappsAssessmentTimeline
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.AssessmentNeedsService
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.AssessmentOffenceService

@RestController
class AssessmentController(
  private val assessmentNeedsService: AssessmentNeedsService,
  private val assessmentOffenceService: AssessmentOffenceService,
) {

  @RequestMapping(path = ["/needs/crn/{crn}"], method = [RequestMethod.GET])
  @Operation(description = "Gets criminogenic needs for crn")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "403", description = "Unauthorized"),
      ApiResponse(responseCode = "404", description = "CRN Not Found"),
      ApiResponse(responseCode = "200", description = "OK"),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_PROBATION', 'ROLE_RISK_INTEGRATIONS_RO', 'ROLE_OFFENDER_RISK_RO')")
  fun getCriminogenicNeedsByCrn(
    @Parameter(description = "CRN", required = true, example = "D1974X")
    @PathVariable
    crn: String,
  ): AssessmentNeedsDto = assessmentNeedsService.getAssessmentNeeds(crn)

  @RequestMapping(path = ["/needs/crn/{crn}/{timeframe}"], method = [RequestMethod.GET])
  @Operation(description = "Gets criminogenic needs for crn within specified timeframe, measured in weeks")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "403", description = "Unauthorized"),
      ApiResponse(responseCode = "404", description = "CRN Not Found"),
      ApiResponse(responseCode = "200", description = "OK"),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_PROBATION', 'ROLE_RISK_INTEGRATIONS_RO', 'ROLE_OFFENDER_RISK_RO')")
  fun getCriminogenicNeedsByCrnWithinTimeframe(
    @Parameter(description = "CRN", required = true, example = "D1974X")
    @Parameter(description = "Timeframe", required = true, example = "70")
    @PathVariable crn: String,
    @PathVariable timeframe: Long,
  ): AssessmentNeedsDto = assessmentNeedsService.getAssessmentNeeds(crn, timeframe)

  @RequestMapping(path = ["/assessments/crn/{crn}/offence"], method = [RequestMethod.GET])
  @Operation(description = "Gets offence details from latest complete assessment for crn")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "403",
        description = "User does not have permission to access offender with provided CRN",
      ),
      ApiResponse(responseCode = "404", description = "Offender does not exist for CRN"),
      ApiResponse(responseCode = "404", description = "Offender does not exist in Delius for provided CRN"),
      ApiResponse(responseCode = "404", description = "User does not exist in Delius for provided user name"),
      ApiResponse(responseCode = "401", description = "Unauthorised"),
      ApiResponse(responseCode = "200", description = "OK"),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_PROBATION')")
  fun getAssessmentOffenceDetails(
    @Parameter(description = "CRN", required = true, example = "D1974X")
    @PathVariable
    crn: String,
  ): AssessmentOffenceDto = assessmentOffenceService.getAssessmentOffence(crn)

  @RequestMapping(path = ["/assessments/crn/{crn}/sexually-motivated-offence"], method = [RequestMethod.GET])
  @Operation(description = "Gets sexually motivated offence details from section 1 of the latest complete assessment")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "404", description = "Assessment does not exist"),
      ApiResponse(responseCode = "403", description = "Missing role: `ROLE_PROBATION` or `ROLE_ARNS__RISKS__RO`"),
      ApiResponse(responseCode = "200", description = "OK"),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_PROBATION', 'ROLE_ARNS__RISKS__RO')")
  fun getAssessmentSexuallyMotivatedOffenceDetails(
    @PathVariable crn: String,
  ) = assessmentOffenceService.getSexuallyMotivatedOffenceDetails(crn)

  @RequestMapping(path = ["/assessments/timeline/{identifierType}/{identifierValue}"], method = [RequestMethod.GET])
  @Operation(description = "Gets assessment timeline for an identifier")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "403", description = "Unauthorized"),
      ApiResponse(responseCode = "404", description = "Not Found"),
      ApiResponse(responseCode = "200", description = "OK"),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_PROBATION', 'ROLE_OFFENDER_RISK_RO')")
  fun getAssessmentTimelineByCrn(
    @PathVariable identifierType: String,
    @PathVariable identifierValue: String,
  ) = assessmentOffenceService.getAssessmentTimeline(PersonIdentifier.from(identifierType, identifierValue))

  @RequestMapping(path = ["/san-indicator/crn/{crn}"], method = [RequestMethod.GET])
  @Operation(description = "Gets san-indicator by CRN")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "403", description = "Unauthorized"),
      ApiResponse(responseCode = "404", description = "Not Found"),
      ApiResponse(responseCode = "200", description = "OK"),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_PROBATION', 'ROLE_OFFENDER_RISK_RO')")
  fun getSanIndicatorByCrn(
    @Parameter(description = "CRN", required = true, example = "D1974X")
    @PathVariable
    crn: String,
  ) = assessmentOffenceService.getSanIndicator(crn)

  @RequestMapping(path = ["/san-indicator/crn/{crn}/{timeframe}"], method = [RequestMethod.GET])
  @Operation(description = "Gets san-indicator by CRN within specified timeframe, measured in weeks")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "403", description = "Unauthorized"),
      ApiResponse(responseCode = "404", description = "Not Found"),
      ApiResponse(responseCode = "200", description = "OK"),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_PROBATION', 'ROLE_OFFENDER_RISK_RO')")
  fun getSanIndicatorByCrnWithinTimeframe(
    @Parameter(description = "CRN", required = true, example = "D1974X")
    @Parameter(description = "Timeframe", required = true, example = "70")
    @PathVariable crn: String,
    @PathVariable timeframe: Long,
  ) = assessmentOffenceService.getSanIndicator(crn, timeframe)

  @RequestMapping(path = ["/assessments/mapps/{identifierType}/{identifierValue}"], method = [RequestMethod.GET])
  @Operation(
    description = """
      Gets latest COMPLETE OASys assessment data for MAPPS external integration.
      
      Returns ALL latest COMPLETE assessments with:
      - Assessment metadata (dates, type, status)
      - Assessor name (always present)
      - Countersigner name (optional - may be null)
      
      This endpoint:
      - Does NOT perform Limited Access Offenders (LAO) checks - bypasses internal authorization
      - Returns all COMPLETE assessments sorted by completion date (latest first)
      - Fetches assessor/countersigner details from section1 endpoint per assessment
      - Skips assessments where section1 data cannot be fetched
      - Is only available to ROLE_ARNS__EXTERNAL_API_RO role
    """,
  )
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "OK - returns list of complete assessments"),
      ApiResponse(responseCode = "403", description = "Unauthorized - insufficient role"),
      ApiResponse(responseCode = "404", description = "No complete assessments found or no section1 data available"),
    ],
  )
  @PreAuthorize("hasRole('ROLE_ARNS__EXTERNAL_API_RO')")
  fun getMappasAssessmentData(
    @Parameter(description = "Identifier type (e.g. crn, pnc)", required = true, example = "crn")
    @PathVariable identifierType: String,
    @Parameter(description = "Identifier value (e.g. X123456)", required = true, example = "X123456")
    @PathVariable identifierValue: String,
  ): MappsAssessmentTimeline = assessmentOffenceService.getLatestCompleteAssessmentsForMapps(
    PersonIdentifier.from(identifierType, identifierValue),
  )
}
