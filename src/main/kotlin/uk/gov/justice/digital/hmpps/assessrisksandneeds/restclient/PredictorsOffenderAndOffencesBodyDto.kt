package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient

import java.time.LocalDate
import java.time.LocalDateTime

data class PredictorsOffenderAndOffencesBodyDto(
  val gender: String,
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
  val mostRecentSexualOffenceDate: LocalDate,
  val totalSexualOffencesInvolvingAnAdult: Int,
  val totalSexualOffencesInvolvingAChild: Int,
  val totalSexualOffencesInvolvingChildImages: Int,
  val totalNonSexualOffences: Int,
  val earliestReleaseDate: LocalDate,
  val dynamicScoringOffences: DynamicScoringOffences?
)

data class DynamicScoringOffences(
  val hasCompletedInterview: Boolean,
  val committedOffenceUsingWeapon: Boolean,
  val hasSuitableAccommodation: Int?,
  val isUnemployed: Int?,
  val currentRelationshipWithPartner: Int?,
  val evidenceOfDomesticViolence: Boolean,
  val isAVictim: Boolean,
  val isAPerpetrator: Boolean,
  val alcoholUseIssues: Int?,
  val bingeDrinkingIssues: Int?,
  val impulsivityIssues: Int?,
  val temperControlIssues: Int?,
  val proCriminalAttitudes: Int?,
  val previousOffences: PreviousOffences?
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

data class CurrentOffence(val offenceCode: String, val offenceSubcode: String)
