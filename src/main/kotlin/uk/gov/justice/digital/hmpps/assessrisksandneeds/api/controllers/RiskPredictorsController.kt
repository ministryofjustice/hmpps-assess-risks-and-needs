package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AllPredictorVersioned
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AllPredictorVersionedDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AllPredictorVersionedLegacyDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.IdentifierType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskScoresDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrPredictorDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrPredictorVersioned
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.RiskPredictorService

@RestController
class RiskPredictorsController(private val riskPredictorService: RiskPredictorService) {
  @Deprecated("Use /risks/predictors/rsr/{identifierType}/{identifierValue}. This endpoint will be removed in a future release.")
  @RequestMapping(path = ["/risks/crn/{crn}/predictors/rsr/history"], method = [RequestMethod.GET])
  @Operation(
    description = """
    Gets RSR score history for a CRN
    **Deprecated endpoint.**
    Please use **/risks/predictors/rsr/{identifierType}/{identifierValue}** instead.
    This endpoint will be removed in a future release.
    """,
    deprecated = true)
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "403", description = "Unauthorized"),
      ApiResponse(responseCode = "200", description = "OK"),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_PROBATION')")
  fun getRsrScoresByCrn(
    @Parameter(description = "CRN", required = true)
    @PathVariable
    crn: String,
  ): List<RsrPredictorDto> {
    log.info("Retrieving RSR score history for crn: $crn")
    return riskPredictorService.getAllRsrHistory(crn)
  }

  @RequestMapping(path = ["/risks/predictors/rsr/{identifierType}/{identifierValue}"], method = [RequestMethod.GET])
  @Operation(description = "Gets RSR scores for an identifier type (e.g. CRN)")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "403", description = "Unauthorized"),
      ApiResponse(responseCode = "200", description = "OK"),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_PROBATION')")
  fun getRsrScoresByIdentifierType(
    @Parameter(description = "Identifier type (e.g. crn)", required = true)
    @PathVariable
    identifierType: IdentifierType,
    @Parameter(description = "Identifier Value", required = true)
    @PathVariable
    identifierValue: String,
  ): List<RsrPredictorVersioned<Any>> = riskPredictorService.getAllRsrScores(identifierType, identifierValue)

  @Deprecated("Use /risks/predictors/all/{identifierType}/{identifierValue}. This endpoint will be removed in a future release.")
  @RequestMapping(path = ["/risks/crn/{crn}/predictors/all"], method = [RequestMethod.GET])
  @Operation(
    description = """
    Gets risk predictors scores for all latest completed assessments from the last 1 year
    **Deprecated endpoint.**
    Please use **/risks/predictors/all/{identifierType}/{identifierValue}** instead.
    This endpoint will be removed in a future release.
    """,
    deprecated = true)
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "403", description = "User does not have permission to access offender with provided CRN"),
      ApiResponse(responseCode = "404", description = "Risk data does not exist for CRN"),
      ApiResponse(responseCode = "404", description = "Offender does not exist in Delius for provided CRN"),
      ApiResponse(responseCode = "404", description = "User does not exist in Delius for provided user name"),
      ApiResponse(responseCode = "401", description = "Unauthorised"),
      ApiResponse(responseCode = "200", description = "OK"),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_PROBATION', 'ROLE_RISK_RESETTLEMENT_PASSPORT_RO', 'ROLE_RISK_INTEGRATIONS_RO', 'ROLE_ACCREDITED_PROGRAMS_RO', 'ROLE_ARNS__MANAGE_PEOPLE_ON_PROBATION__RO')")
  fun getAllRiskScores(@PathVariable crn: String): List<RiskScoresDto> {
    log.info("Entered getAllRiskScores for crn: $crn")
    return riskPredictorService.getAllRiskScores(crn)
  }

  @RequestMapping(path = ["/risks/predictors/all/{identifierType}/{identifierValue}"], method = [RequestMethod.GET])
  @Operation(description = "Gets risk predictors scores for all latest completed assessments")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "403", description = "User does not have permission to access offender with provided CRN"),
      ApiResponse(responseCode = "404", description = "Risk data does not exist for CRN"),
      ApiResponse(responseCode = "404", description = "Offender does not exist in Delius for provided CRN"),
      ApiResponse(responseCode = "404", description = "User does not exist in Delius for provided user name"),
      ApiResponse(responseCode = "401", description = "Unauthorised"),
      ApiResponse(responseCode = "400", description = "Bad request"),
      ApiResponse(responseCode = "200", description = "OK"),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_PROBATION', 'ROLE_RISK_RESETTLEMENT_PASSPORT_RO', 'ROLE_RISK_INTEGRATIONS_RO', 'ROLE_ACCREDITED_PROGRAMS_RO', 'ROLE_ARNS__MANAGE_PEOPLE_ON_PROBATION__RO')")
  fun getAllRiskScoresVersioned(
    @Parameter(description = "Identifier type (e.g. crn)", required = true)
    @PathVariable
    identifierType: IdentifierType,
    @Parameter(description = "Identifier Value", required = true)
    @PathVariable
    identifierValue: String,
  ): List<AllPredictorVersioned<Any>> = riskPredictorService.getAllRiskScores(identifierType, identifierValue)

  @RequestMapping(path = ["/assessments/id/{id}/risk/predictors/all"], method = [RequestMethod.GET])
  @Operation(description = "Gets risk predictors scores for the requested assessment ID")
  @Schema(oneOf = [AllPredictorVersionedLegacyDto::class, AllPredictorVersionedDto::class])
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "403", description = "User does not have permission to access assessment with provided ID"),
      ApiResponse(responseCode = "404", description = "Risk data does not exist for assessment ID"),
      ApiResponse(responseCode = "401", description = "Unauthorised"),
      ApiResponse(responseCode = "400", description = "Bad request"),
      ApiResponse(responseCode = "200", description = "OK"),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_PROBATION_API__ACCREDITED_PROGRAMMES__ASSESSMENT')")
  fun getRiskScoresByAssessmentId(
    @Parameter(description = "Assessment ID", required = true)
    @PathVariable
    id: Long,
  ): AllPredictorVersioned<Any> = riskPredictorService.getAllRiskScoresByAssessmentId(id)

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }
}
