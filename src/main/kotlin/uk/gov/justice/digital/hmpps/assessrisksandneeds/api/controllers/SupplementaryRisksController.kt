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
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.CreateSupplementaryRiskDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.SupplementaryRiskDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.SupplementaryRiskService
import java.util.UUID

@RestController
class SupplementaryRisksController(
  val supplementaryRiskService: SupplementaryRiskService,
) {
  @RequestMapping(path = ["/risks/supplementary/{supplementaryRiskId}"], method = [RequestMethod.GET])
  @Operation(description = "Gets supplementary risk for a given ID")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "403", description = "Unauthorized"),
      ApiResponse(responseCode = "404", description = "Supplementary risk source Not Found"),
      ApiResponse(responseCode = "200", description = "OK"),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_PROBATION', 'ROLE_CRS_PROVIDER')")
  fun getSupplementaryRiskById(
    @Parameter(description = "Supplementary ID", required = true, example = "78beac68-884c-4784-9bea-fd8088f52a47")
    @PathVariable
    supplementaryRiskId: UUID,
  ): SupplementaryRiskDto = supplementaryRiskService.getRiskBySupplementaryRiskUuid(supplementaryRiskId)

  @RequestMapping(path = ["/risks/supplementary"], method = [RequestMethod.POST])
  @Operation(description = "Create new supplementary risk for a given source")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "403", description = "Unauthorized"),
      ApiResponse(responseCode = "200", description = "OK"),
    ],
  )
  @PreAuthorize("hasAnyRole('ROLE_PROBATION')")
  fun createSupplementaryRisk(
    @Parameter(description = "Supplementary Risk", required = true)
    @RequestBody
    supplementaryRisk: CreateSupplementaryRiskDto,
  ): SupplementaryRiskDto = supplementaryRiskService.createNewSupplementaryRisk(supplementaryRisk)
}
