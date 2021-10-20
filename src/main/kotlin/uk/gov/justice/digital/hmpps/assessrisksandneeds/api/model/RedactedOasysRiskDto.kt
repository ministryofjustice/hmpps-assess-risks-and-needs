package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import io.swagger.v3.oas.annotations.media.Schema

data class RedactedOasysRiskDto(
  @Schema(description = "Question corresponding to OASys ROSH 10.1, 'Who is at risk'", example = "Free text up to 4000 characters")
  val riskWho: String? = null,

  @Schema(description = "Question corresponding to OASys ROSH 10.3, 'When is the risk likely to be greatest'", example = "Free text up to 4000 characters")
  val riskWhen: String? = null,

  @Schema(description = "Question corresponding to OASys ROSH 10.2, 'What is the nature of the risk'", example = "Free text up to 4000 characters")
  val riskNature: String? = null,

  @Schema(description = "Question corresponding to OASys ROSH 3.2, 'Concerns in relation to self harm'", example = "Free text up to 4000 characters")
  val concernsSelfHarm: String? = null,

  @Schema(description = "Question corresponding to OASys ROSH 3.1, 'Concerns in relation to suicide'", example = "Free text up to 4000 characters")
  val concernsSuicide: String? = null,

  @Schema(description = "Question corresponding to OASys ROSH 3.3, 'Concerns in relation to coping in a hostel setting'", example = "Free text up to 4000 characters")
  val concernsHostel: String? = null,

  @Schema(description = "Question corresponding to OASys ROSH 3.4, 'Concerns in relation to vulnerability'", example = "Free text up to 4000 characters")
  val concernsVulnerability: String? = null,
)
