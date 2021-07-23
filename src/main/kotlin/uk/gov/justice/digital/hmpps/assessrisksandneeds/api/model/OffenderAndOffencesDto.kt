package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import java.time.LocalDate
import java.time.LocalDateTime

data class OffenderAndOffencesDto(
  val sex: Sex,
  val dob: LocalDate,
  val assessmentDate: LocalDateTime,
  val currentOffence: CurrentOffence,
  val dateOfFirstSanction: LocalDate,
  val ageAtFirstSanction: Int,
  val totalOffences: Int,
  val totalViolentOffences: Int,
  val dateOfCurrentConviction: LocalDate,
  val hasAnySexualOffences: Boolean,
  val isCurrentSexualOffence: Boolean,
  val isCurrentOffenceVictimStranger: Boolean,
  val mostRecentSexOffenceDate: LocalDate,
  val totalSexualOffencesInvolvingAnAdult: Int,
  val totalSexualOffencesInvolvingAChild: Int,
  val totalSexualOffencesInvolvingChildImages: Int,
  val totalNonSexualOffences: Int,
  val earliestReleaseDate: LocalDateTime,
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