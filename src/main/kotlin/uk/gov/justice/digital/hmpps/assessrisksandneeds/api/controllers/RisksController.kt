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
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AllRoshRiskDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OtherRoshRisksDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ResponseDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskRoshSummaryDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RoshRiskToSelfDto

@RestController
class RisksController {

  @RequestMapping(path = ["/risks/crn/{crn}/summary"], method = [RequestMethod.GET])
  @Operation(description = "Gets rosh summary for crn")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "403", description = "Unauthorized"),
      ApiResponse(responseCode = "404", description = "CRN Not Found"),
      ApiResponse(responseCode = "200", description = "OK")
    ]
  )
  @PreAuthorize("hasAnyRole('ROLE_PROBATION', 'ROLE_CRS_PROVIDER') and hasAuthority('SCOPE_read')")
  fun getRiskSummaryByCrn(
    @Parameter(description = "CRN", required = true, example = "D1974X")
    @PathVariable crn: String,
  ): RiskRoshSummaryDto {
    return RiskRoshSummaryDto(
      "whoisAtRisk",
      "natureOfRisk",
      "riskImminence",
      "riskIncreaseFactors",
      "riskMitigationFactors",
      mapOf(RiskLevel.HIGH to listOf("children")),
      mapOf(
        RiskLevel.MEDIUM to listOf("known adult"),
        RiskLevel.VERY_HIGH to listOf("public", "staff")
      )
    )
  }

  @RequestMapping(path = ["/risks/crn/{crn}/self"], method = [RequestMethod.GET])
  @Operation(description = "Gets rosh to individual for crn")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "403", description = "Unauthorized"),
      ApiResponse(responseCode = "404", description = "CRN Not Found"),
      ApiResponse(responseCode = "200", description = "OK")
    ]
  )
  @PreAuthorize("hasAnyRole('ROLE_PROBATION', 'ROLE_CRS_PROVIDER') and hasAuthority('SCOPE_read')")
  fun getRiskToSelfByCrn(
    @Parameter(description = "CRN", required = true, example = "D1974X")
    @PathVariable crn: String,
  ): RoshRiskToSelfDto {
    return RoshRiskToSelfDto(
      RiskDto(ResponseDto.DK, "Previous concerns", "Current concerns"),
      RiskDto(ResponseDto.NO, "Previous concerns", "Current concerns"),
      RiskDto(ResponseDto.YES, "Previous concerns", "Current concerns"),
      RiskDto(ResponseDto.DK, "Previous concerns", "Current concerns"),
      RiskDto(null, null, null),
    )
  }

  @RequestMapping(path = ["/risks/crn/{crn}/other"], method = [RequestMethod.GET])
  @Operation(description = "Gets other rosh risks for crn")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "403", description = "Unauthorized"),
      ApiResponse(responseCode = "404", description = "CRN Not Found"),
      ApiResponse(responseCode = "200", description = "OK")
    ]
  )
  @PreAuthorize("hasAnyRole('ROLE_PROBATION', 'ROLE_CRS_PROVIDER') and hasAuthority('SCOPE_read')")
  fun getOtherRisksByCrn(
    @Parameter(description = "CRN", required = true, example = "D1974X")
    @PathVariable crn: String,
  ): OtherRoshRisksDto {
    return OtherRoshRisksDto(
      RiskDto(ResponseDto.YES, "Previous concerns", "Current concerns"),
      RiskDto(ResponseDto.DK, "Previous concerns", "Current concerns"),
      RiskDto(null, null, null),
    )
  }

  @RequestMapping(path = ["/risks/crn/{crn}"], method = [RequestMethod.GET])
  @Operation(description = "Gets other rosh risks for crn")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "403", description = "Unauthorized"),
      ApiResponse(responseCode = "404", description = "CRN Not Found"),
      ApiResponse(responseCode = "200", description = "OK")
    ]
  )
  @PreAuthorize("hasAnyRole('ROLE_PROBATION', 'ROLE_CRS_PROVIDER') and hasAuthority('SCOPE_read')")
  fun getRoshRisksByCrn(
    @Parameter(description = "CRN", required = true, example = "D1974X")
    @PathVariable crn: String,
  ): AllRoshRiskDto {
    return AllRoshRiskDto(
      RoshRiskToSelfDto(
        RiskDto(ResponseDto.DK, "Previous concerns", "Current concerns"),
        RiskDto(ResponseDto.NO, "Previous concerns", "Current concerns"),
        RiskDto(ResponseDto.YES, "Previous concerns", "Current concerns"),
        RiskDto(ResponseDto.DK, "Previous concerns", "Current concerns"),
        RiskDto(null, null, null),
      ),
      OtherRoshRisksDto(
        RiskDto(ResponseDto.YES, "Previous concerns", "Current concerns"),
        RiskDto(ResponseDto.DK, "Previous concerns", "Current concerns"),
        RiskDto(null, null, null)
      ),
      RiskRoshSummaryDto(
        "whoisAtRisk",
        "natureOfRisk",
        "riskImminence",
        "riskIncreaseFactors",
        "riskMitigationFactors",
        mapOf(RiskLevel.HIGH to listOf("children")),
        mapOf(RiskLevel.MEDIUM to listOf("known adult"))
      )
    )
  }
}
