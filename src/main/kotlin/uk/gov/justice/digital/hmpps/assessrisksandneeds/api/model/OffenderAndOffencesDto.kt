package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalDateTime

data class OffenderAndOffencesDto(
  @Schema(description = "Offender CRN", example = "DX12340A")
  val crn: String,

  @Schema(description = "Gender", example = "MALE, FEMALE")
  val gender: Gender,

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

  @Schema(description = "Offences for dynamic scoring")
  val dynamicScoringOffences: DynamicScoringOffences?
)

data class DynamicScoringOffences(
  @Schema(description = "Have you completed an interview with the individual?", example = "true")
  val hasCompletedInterview: Boolean,

  @Schema(description = "Did the offence involve carrying or use of a weapon?", example = "true")
  val committedOffenceUsingWeapon: Boolean,

  @Schema(description = "Is the individual living in suitable accommodation?", example = "NO_PROBLEMS, SOME_PROBLEMS, SIGNIFICANT_PROBLEMS, MISSING")
  val hasSuitableAccommodation: ProblemsLevel?,

  @Schema(description = "Is the person unemployed or will be unemployed upon release?", example = "NO, NOT_AVAILABLE_FOR_WORK, YES, MISSING")
  val isUnemployed: EmploymentType?,

  @Schema(description = "What is the person's current relationship with their partner?", example = "NO_PROBLEMS, SOME_PROBLEMS, SIGNIFICANT_PROBLEMS, MISSING")
  val currentRelationshipWithPartner: ProblemsLevel?,

  @Schema(description = "Is there evidence that the individual is a perpetrator of domestic abuse?", example = "true")
  val evidenceOfDomesticViolence: Boolean,

  @Schema(description = "Is the individual a victim of domestic abuse?", example = "true")
  val isAVictim: Boolean,

  @Schema(description = "Is the individual a perpetrator of domestic abuse?", example = "true")
  val isAPerpetrator: Boolean,

  @Schema(description = "Is the person's current use of alcohol a problem?", example = "NO_PROBLEMS, SOME_PROBLEMS, SIGNIFICANT_PROBLEMS, MISSING")
  val alcoholUseIssues: ProblemsLevel?,

  @Schema(description = "Is there evidence of binge drinking or excessive use of alcohol in the last 6 months?", example = "NO_PROBLEMS, SOME_PROBLEMS, SIGNIFICANT_PROBLEMS, MISSING")
  val bingeDrinkingIssues: ProblemsLevel?,

  @Schema(description = "Is impulsivity a problem for the individual?", example = "NO_PROBLEMS, SOME_PROBLEMS, SIGNIFICANT_PROBLEMS, MISSING")
  val impulsivityIssues: ProblemsLevel?,

  @Schema(description = "Is temper control a problem for the individual?", example = "NO_PROBLEMS, SOME_PROBLEMS, SIGNIFICANT_PROBLEMS, MISSING")
  val temperControlIssues: ProblemsLevel?,

  @Schema(description = "Does the individual have pro-criminal attitudes?", example = "NO_PROBLEMS, SOME_PROBLEMS, SIGNIFICANT_PROBLEMS, MISSING")
  val proCriminalAttitudes: ProblemsLevel?,

  @Schema(description = "Previous Offences")
  val previousOffences: PreviousOffences?
)

data class PreviousOffences(
  @Schema(description = "Murder/attempted murder/threat or conspiracy to murder/manslaughter", example = "true")
  val murderAttempt: Boolean,

  @Schema(description = "Wounding/GBH (Sections 18/20 Offences Against the Person Act 1861)", example = "true")
  val wounding: Boolean,

  @Schema(description = "Aggravated burglary", example = "true")
  val aggravatedBurglary: Boolean,

  @Schema(description = "Arson", example = "true")
  val arson: Boolean,

  @Schema(description = "Criminal damage with intent to endanger life", example = "true")
  val criminalDamage: Boolean,

  @Schema(description = "Kidnapping/false imprisonment", example = "true")
  val kidnapping: Boolean,

  @Schema(description = "Possession of a firearm with intent to endanger life or resist arrest", example = "true")
  val firearmPossession: Boolean,

  @Schema(description = "Robbery", example = "true")
  val robbery: Boolean,

  @Schema(description = "Any other offence involving possession and/or use of weapons", example = "true")
  val offencesWithWeapon: Boolean
)

data class CurrentOffence(val offenceCode: String, val offenceSubcode: String)

enum class EmploymentType(val score: Int? = null) {
  NO(0), NOT_AVAILABLE_FOR_WORK(0), YES(2), MISSING
}

enum class Gender {
  MALE, FEMALE
}

enum class ProblemsLevel(val score: Int? = null) {
  NO_PROBLEMS(0), SOME_PROBLEMS(1), SIGNIFICANT_PROBLEMS(2), MISSING
}
