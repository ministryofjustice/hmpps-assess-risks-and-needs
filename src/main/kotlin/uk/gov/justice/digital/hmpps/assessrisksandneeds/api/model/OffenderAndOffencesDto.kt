package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalDateTime

data class OffenderAndOffencesDto(
  @Schema(description = "Sex", example = "MALE, FEMALE")
  val sex: Sex,

  @Schema(description = "date of birth", example = "1980-01-01")
  @JsonFormat(pattern = "yyyy-MM-dd")
  val dob: LocalDate,

  @Schema(description = "Assessment date", example = "2021-01-01 19:24:45")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  val assessmentDate: LocalDateTime,

  @Schema(description = "Current Offence")
  val currentOffence: CurrentOffence,

  @Schema(description = "Date of first sanction for the offender", example = "2000-01-01")
  @JsonFormat(pattern = "yyyy-MM-dd")
  val dateOfFirstSanction: LocalDate,

  @Schema(description = "Age at first conviction, conditional or absolute discharge in years", example = "19")
  val ageAtFirstSanction: Int,

  @Schema(description = "Total number of sanctions for all offences", example = "10")
  val totalOffences: Int,

  @Schema(description = "How many of the total number of sanctions involved violent offences", example = "10")
  val totalViolentOffences: Int,

  @Schema(description = "Date of current conviction", example = "2020-01-01")
  @JsonFormat(pattern = "yyyy-MM-dd")
  val dateOfCurrentConviction: LocalDate,

  @Schema(description = "Have they ever committed a sexual offence?", example = "true")
  val hasAnySexualOffences: Boolean,

  @Schema(description = "Does the current offence have a sexual motivation?", example = "true")
  val isCurrentSexualOffence: Boolean,

  @Schema(description = "Does the current offence involve a victim who was a stranger?", example = "true")
  val isCurrentOffenceVictimStranger: Boolean,

  @Schema(
    description = "Date of most recent sanction involving a sexual or sexually motivated offence",
    example = "2020-01-01"
  )
  @JsonFormat(pattern = "yyyy-MM-dd")
  val mostRecentSexualOffenceDate: LocalDate,

  @Schema(
    description = "Number of previous or current sanctions involving contact adult sexual or sexually motivated offences",
    example = "5"
  )
  val totalSexualOffencesInvolvingAnAdult: Int,

  @Schema(
    description = "Number of previous or current sanctions involving contact child sexual or sexually motivated offences",
    example = "5"
  )
  val totalSexualOffencesInvolvingAChild: Int,

  @Schema(
    description = "Number of previous or current sanctions involving indecent child image sexual or sexually motivated offences",
    example = "5"
  )
  val totalSexualOffencesInvolvingChildImages: Int,

  @Schema(
    description = "Number of previous or current sanctions involving other non-contact sexual or sexually motivated offences",
    example = "5"
  )
  val totalNonSexualOffences: Int,

  @Schema(
    description = "Date of commencement of community sentence or earliest possible release from custody",
    example = "2025-01-01"
  )
  @JsonFormat(pattern = "yyyy-MM-dd")
  val earliestReleaseDate: LocalDate,

  @Schema(description = "Offences for dynamic scoring",)
  val dynamicScoringOffences: DynamicScoringOffences
)

data class DynamicScoringOffences(
  val hasCompletedInterview: Boolean,
  val committedOffenceUsingWeapon: Boolean,
  val hasSuitableAccommodation: ProblemsLevel?,
  val isUnemployed: EmploymentType?,
  val currentRelationshipWithPartner: String,
  val evidenceOfDomesticViolence: Boolean,
  val isAVictim: Boolean,
  val isAPerpetrator: Boolean,
  val alcoholUseIssues: ProblemsLevel,
  val bingeDrinkingIssues: ProblemsLevel,
  val impulsivityIssues: ProblemsLevel,
  val temperControlIssues: ProblemsLevel,
  val proCriminalAttitudes: ProblemsLevel,
  val previousOffences: PreviousOffences
)

data class PreviousOffences(
  val murderAttempt: Boolean,
  val wounding: Boolean,
  val aggravatedBurglary: Boolean,
  val arson: Boolean,
  val criminalDamage: Boolean,
  val kidnapping: Boolean,
  val firearmPossession: Boolean,
  val robbery: Boolean,
  val offencesWithWeapon: Boolean
)

data class CurrentOffence(val currentOffenceCode: String, val currentOffenceSubcode: String)

enum class EmploymentType {
  NO, NOT_AVAILABLE_FOR_WORK, YES, MISSING
}

enum class Sex {
  MALE, FEMALE
}

enum class ProblemsLevel {
  NO_PROBLEMS, SOME_PROBLEMS, SIGNIFICANT_PROBLEMS, MISSING
}
