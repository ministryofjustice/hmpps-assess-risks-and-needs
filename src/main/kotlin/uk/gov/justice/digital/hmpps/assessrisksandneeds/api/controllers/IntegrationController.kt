package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.controllers

import com.fasterxml.jackson.annotation.JsonView
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AllPredictorVersioned
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AllRoshRiskDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentNeedsDetailsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.IdentifierType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PersonIdentifier
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskManagementPlansDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskScoresDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.View
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.MappsAssessmentTimeline
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.AssessmentNeedsService
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.AssessmentOffenceService
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.DEFAULT_TIMEFRAME_WEEKS
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.RiskManagementPlanService
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.RiskPredictorService
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.RiskService

@RestController
class IntegrationController(
  private val riskPredictorService: RiskPredictorService,
  private val riskService: RiskService,
  private val needsService: AssessmentNeedsService,
  private val riskManagementPlanService: RiskManagementPlanService,
  private val assessmentOffenceService: AssessmentOffenceService,
) {
  @Deprecated("Use /risks/predictors/unsafe/all/{identifierType}/{identifierValue}. This endpoint will be removed in a future release.")
  @RequestMapping(path = ["/risks/predictors/{crn}"], method = [RequestMethod.GET])
  @Operation(
    description = """
    Gets risk predictors scores for all latest completed assessments from the last 1 year
    Deprecated endpoint.
    Please use /risks/predictors/all/{identifierType}/{identifierValue} instead.
    This endpoint will be removed in a future release.
    """,
    deprecated = true,
  )
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

  @RequestMapping(path = ["/risks/predictors/unsafe/all/{identifierType}/{identifierValue}"], method = [RequestMethod.GET])
  @Operation(description = GET_ALL_RISK_SCORES_BY_IDENTIFIER_TYPE_DESC)
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "403", description = "User does not have permission to access offender with provided CRN"),
      ApiResponse(responseCode = "404", description = "Risk data does not exist for CRN"),
      ApiResponse(responseCode = "404", description = "Offender does not exist in Delius for provided CRN"),
      ApiResponse(responseCode = "404", description = "User does not exist in Delius for provided user name"),
      ApiResponse(responseCode = "401", description = "Unauthorised"),
      ApiResponse(responseCode = "400", description = "Bad request"),
      ApiResponse(
        responseCode = "200",
        description = "OK",
        content = [
          Content(
            array = ArraySchema(schema = Schema(ref = "AllPredictorVersionedUnion")),
            mediaType = "application/json",
            examples = [
              ExampleObject(
                name = "List of completed assessments containing both Legacy and New predictor score formats.",
                summary = "Completed assessments and associated risk predictor scores",
                value = GET_ALL_RISK_SCORES_BY_IDENTIFIER_TYPE_EXAMPLE,
              ),
            ],
          ),
        ],
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_ARNS__RISKS__RO')")
  fun getAllRiskScoresVersioned(
    @Parameter(description = "Identifier type (e.g. crn)", required = true)
    @PathVariable
    identifierType: IdentifierType,
    @Parameter(description = "Identifier Value", required = true)
    @PathVariable
    identifierValue: String,
    @Parameter(description = "Include standalone assessments. Defaults to false.", required = false)
    @RequestParam(defaultValue = "false")
    includeStandaloneAssessments: Boolean = false,
  ): List<AllPredictorVersioned<Any>> = riskPredictorService.getAllRiskScoresWithoutLaoCheck(identifierType, identifierValue, includeStandaloneAssessments)

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
    @Parameter(description = TIMEFRAME_QUERY_PARAM_DESC, `in` = ParameterIn.QUERY, example = "70")
    @RequestParam(required = false)
    timeframe: Long = DEFAULT_TIMEFRAME_WEEKS,
  ): AllRoshRiskDto = riskService.getRoshRisksWithoutLaoCheck(crn, timeframe)

  @Deprecated("Use /risks/rosh/{crn}?timeframe={timeframe}. This endpoint will be removed in a future release.")
  @RequestMapping(path = ["/risks/rosh/{crn}/{timeframe}"], method = [RequestMethod.GET])
  @Operation(
    description = """
    Gets ROSH risks for crn. Only returns freeform text concerns for risk to self where answer to corresponding risk question is Yes.
    Returns only assessments completed within specified timeframe, measured in weeks.
    Deprecated endpoint.
    Please use /risks/rosh/{crn}?timeframe={timeframe} instead.
    This endpoint will be removed in a future release.
    """,
    deprecated = true,
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
    @PathVariable crn: String,
    @Parameter(description = "Timeframe", required = true, example = "70")
    @PathVariable timeframe: Long,
  ): AllRoshRiskDto = riskService.getRoshRisksWithoutLaoCheck(crn, timeframe)

  @RequestMapping(path = ["/needs/{crn}"], method = [RequestMethod.GET])
  @Operation(description = "Gets criminogenic needs for crn")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "403", description = "Unauthorized"),
      ApiResponse(responseCode = "404", description = "CRN Not Found"),
      ApiResponse(
        responseCode = "200",
        description = "OK",
        content = [
          Content(
            schema = Schema(implementation = AssessmentNeedsDetailsDto::class),
            mediaType = "application/json",
            examples = [
              ExampleObject(
                name = "OASys assessment needs data",
                summary = "Completed OASys strengths and needs assessment",
                value = GET_NEEDS_BY_CRN_OASYS_ASSESSMENT_VERSION,
              ),
              ExampleObject(
                name = "SAN assessment needs data",
                summary = "Completed SAN strengths and needs assessment",
                value = GET_NEEDS_BY_CRN_SAN_ASSESSMENT_VERSION,
              ),
            ],
          ),
        ],
      ),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_ARNS__RISKS__RO', 'ROLE_SENTENCE_PLAN_READ')")
  fun getCriminogenicNeedsByCrn(
    @Parameter(description = "CRN", required = true, example = "D1974X")
    @PathVariable
    crn: String,
    @Parameter(description = TIMEFRAME_QUERY_PARAM_DESC, `in` = ParameterIn.QUERY, example = "70")
    @RequestParam(required = false)
    timeframe: Long = DEFAULT_TIMEFRAME_WEEKS,
    @Parameter(
      description = "Exclude incomplete assessments",
      `in` = ParameterIn.QUERY,
      example = "false",
    )
    excludeIncomplete: Boolean = true,
  ): AssessmentNeedsDetailsDto = needsService.getAssessmentNeedsDetails(crn, timeframe, excludeIncomplete)

  @Deprecated("Use /needs/{crn}?timeframe={timeframe}. This endpoint will be removed in a future release.")
  @RequestMapping(path = ["/needs/{crn}/{timeframe}"], method = [RequestMethod.GET])
  @Operation(
    description = """
    Gets criminogenic needs for crn within specified timeframe, measured in weeks.
    Deprecated endpoint.
    Please use /needs/{crn}?timeframe={timeframe} instead.
    This endpoint will be removed in a future release.
    """,
    deprecated = true,
  )
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "403", description = "Unauthorized"),
      ApiResponse(responseCode = "404", description = "CRN Not Found"),
      ApiResponse(
        responseCode = "200",
        description = "OK",
        content = [
          Content(
            schema = Schema(implementation = AssessmentNeedsDetailsDto::class),
            mediaType = "application/json",
            examples = [
              ExampleObject(
                name = "OASys assessment needs data",
                summary = "Completed OASys strengths and needs assessment",
                value = GET_NEEDS_BY_CRN_OASYS_ASSESSMENT_VERSION,
              ),
              ExampleObject(
                name = "SAN assessment needs data",
                summary = "Completed SAN strengths and needs assessment",
                value = GET_NEEDS_BY_CRN_SAN_ASSESSMENT_VERSION,
              ),
            ],
          ),
        ],
      ),
    ],
  )
  @PreAuthorize("hasRole('ROLE_ARNS__RISKS__RO')")
  fun getCriminogenicNeedsByCrnWithinTimeframe(
    @Parameter(description = "CRN", required = true, example = "D1974X")
    @PathVariable crn: String,
    @Parameter(description = "Timeframe", required = true, example = "70")
    @PathVariable timeframe: Long,
    @Parameter(description = "Exclude incomplete assessments", `in` = ParameterIn.QUERY, example = "false")
    excludeIncomplete: Boolean = true,
  ): AssessmentNeedsDetailsDto = needsService.getAssessmentNeedsDetails(crn, timeframe, excludeIncomplete)

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
  fun getMappsAssessmentData(
    @Parameter(description = "Identifier type (e.g. crn, pnc)", required = true, example = "crn")
    @PathVariable identifierType: String,
    @Parameter(description = "Identifier value (e.g. X123456)", required = true, example = "X123456")
    @PathVariable identifierValue: String,
  ): MappsAssessmentTimeline = assessmentOffenceService.getAssessmentsForMapps(
    PersonIdentifier.from(identifierType, identifierValue),
  )
}
