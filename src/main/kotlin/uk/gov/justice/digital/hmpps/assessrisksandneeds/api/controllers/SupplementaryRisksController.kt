package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.SupplementaryRiskDto
import java.util.UUID

@RestController
class SupplementaryRisksController {

  @RequestMapping(path = ["/risks/supplementary/{sourceId}"], method = [RequestMethod.GET])
  @Operation(description = "Gets supplementary risk for a given source")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "403", description = "Unauthorized"),
      ApiResponse(responseCode = "404", description = "Supplementary risk source Not Found"),
      ApiResponse(responseCode = "200", description = "OK")
    ]
  )
  fun getSupplementaryRisk(
    @Parameter(description = "Source ID", required = true, example = "78beac68-884c-4784-9bea-fd8088f52a47")
    @PathVariable sourceId: UUID
  ) {
    TODO()
  }

  @RequestMapping(path = ["/risks/supplementary"], method = [RequestMethod.POST])
  @Operation(description = "Create new supplementary risk for a given source")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "403", description = "Unauthorized"),
      ApiResponse(responseCode = "200", description = "OK")
    ]
  )
  fun createSupplementaryRisk(
    @Parameter(description = "Supplementary Risk", required = true)
    @RequestBody supplementaryRisk: SupplementaryRiskDto
  ) {
    TODO()
  }
}
