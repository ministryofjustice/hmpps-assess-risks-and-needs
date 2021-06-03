package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ErrorResponse(
  @Schema(required = true, description = "Status of Error Code", example = "400")
  val status: Int? = 0,

  @Schema(required = false, description = "Developer Information message", example = "System is down")
  val developerMessage: String? = null,

  @Schema(required = true, description = "Internal Error Code", example = "20012")
  val errorCode: Int? = null,

  @Schema(required = true, description = "Error message information", example = "Prisoner Not Found")
  val userMessage: String? = null,

  @Schema(required = false, description = "Additional information about the error", example = "Hard disk failure")
  val moreInfo: String? = null
)
