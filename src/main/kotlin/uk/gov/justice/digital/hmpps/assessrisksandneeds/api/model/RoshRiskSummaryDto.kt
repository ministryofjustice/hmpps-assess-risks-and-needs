package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import com.fasterxml.jackson.annotation.JsonView
import io.swagger.v3.oas.annotations.media.Schema

data class RiskRoshSummaryDto(

  @Schema(description = "Who is at risk?", example = "X, Y and Z are at risk")
  @JsonView(View.Probation::class)
  val whoIsAtRisk: String? = null,

  @Schema(description = "What is the nature of the risk?", example = "The nature of the risk is X")
  @JsonView(View.Probation::class)
  val natureOfRisk: String? = null,

  @Schema(
    description = "When is the risk likely to be greatest. Consider the timescale and indicate whether risk is immediate or not. " +
      "Consider the risks in custody as well as on release.",
    example = "the risk is imminent and more probably in X situation"
  )
  @JsonView(View.Probation::class)
  val riskImminence: String? = null,

  @Schema(
    description = "What circumstances are likely to increase risk." +
      " Describe factors, actions, events which might increase level of risk, now and in the future.",
    example = "If offender in situation X the risk can be higher"
  )
  @JsonView(View.Probation::class)
  val riskIncreaseFactors: String? = null,

  @Schema(
    description = "What factors are likely to reduce the risk. Describe factors, actions, and events which may reduce " +
      "or contain the level of risk. What has previously stopped him / her?",
    example = "Giving offender therapy in X will reduce the risk"
  )
  @JsonView(View.Probation::class)
  val riskMitigationFactors: String? = null,

  @Schema(
    description = "Assess the risk of serious harm the offender poses in the community",
    example = " " +
      "{" +
      "    \"high \": [\"children\",\"public\",\"know adult\"]," +
      "    \"medium\": [ \"staff\"]," +
      "    \"low\": [\"prisoners\"]" +
      "}"
  )
  @JsonView(View.CrsProvider::class)
  val riskInCommunity: Map<RiskLevel, List<String>> = hashMapOf(),

  @Schema(
    description = "Assess the risk of serious harm the offender poses on the basis that they could be released imminently back into the community." +
      "Assess both the risk of serious harm the offender presents now, in custody, and the risk they could present to others whilst in a custodial setting.",
    example = " " +
      "{" +
      "    \"high \": [\"know adult\"]," +
      "    \"medium\": [ \"staff\", \"prisoners\"]," +
      "    \"low\": [\"children\",\"public\"]" +
      "}"
  )
  @JsonView(View.Probation::class)
  val riskInCustody: Map<RiskLevel, List<String>> = hashMapOf(),

)

enum class RiskLevel(

  val value: String
) {
  VERY_HIGH("Very High"), HIGH("High"), MEDIUM("Medium"), LOW("Low");

  companion object {
    fun fromString(enumValue: String?): RiskLevel {
      return values().firstOrNull { it.value == enumValue }
        ?: throw IllegalArgumentException("Unknown Risk Level $enumValue")
    }
  }
}
