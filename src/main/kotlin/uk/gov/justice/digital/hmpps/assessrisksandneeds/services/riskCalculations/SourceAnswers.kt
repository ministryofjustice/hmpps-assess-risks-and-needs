package uk.gov.justice.digital.hmpps.assessrisksandneeds.services.riskCalculations

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.EmploymentType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.Gender
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ProblemsLevel
import java.time.LocalDate
import java.time.LocalDateTime

class SourceAnswers(
  val gender: Gender,
  val dob: LocalDate,
  @JsonProperty("assessment_date")
  val assessmentDate: LocalDateTime,
  @JsonProperty("offence_code")
  val offenceCode: String,
  @JsonProperty("offence_subcode")
  val offenceSubcode: String,
  @JsonFormat(pattern = "yyyy-MM-dd")
  @JsonProperty("date_first_sanction")
  val dateOfFirstSanction: LocalDate,
  @JsonProperty("total_sanctions")
  val totalOffences: Int,
  @JsonProperty("total_violent_offences")
  val totalViolentOffences: Int,
  @JsonFormat(pattern = "yyyy-MM-dd")
  @JsonProperty("date_current_conviction")
  val dateOfCurrentConviction: LocalDate,
  @JsonProperty("any_sexual_offences")
  val hasAnySexualOffences: Boolean,
  @JsonProperty("current_sexual_offence")
  val isCurrentSexualOffence: Boolean?,
  @JsonProperty("current_offence_victim_stranger")
  val isCurrentOffenceVictimStranger: Boolean?,
  @JsonFormat(pattern = "yyyy-MM-dd")
  @JsonProperty("most_recent_sexual_offence_date")
  val mostRecentSexualOffenceDate: LocalDate?,
  @JsonProperty("total_sexual_offences_adult")
  val totalSexualOffencesInvolvingAnAdult: Int?,
  @JsonProperty("total_sexual_offences_child")
  val totalSexualOffencesInvolvingAChild: Int?,
  @JsonProperty("total_sexual_offences_child_image")
  val totalSexualOffencesInvolvingChildImages: Int?,
  @JsonProperty("total_non_contact_sexual_offences")
  val totalNonContactSexualOffences: Int?,
  @JsonFormat(pattern = "yyyy-MM-dd")
  @JsonProperty("earliest_release_date")
  val earliestReleaseDate: LocalDate,
  @JsonProperty("completed_interview")
  val hasCompletedInterview: Boolean,
  @JsonProperty("suitable_accommodation")
  val hasSuitableAccommodation: ProblemsLevel?,
  @JsonProperty("unemployed_on_release")
  val employment: EmploymentType?,
  @JsonProperty("current_relationship_with_partner")
  val currentRelationshipWithPartner: ProblemsLevel?,
  @JsonProperty("evidence_domestic_violence")
  val evidenceOfDomesticViolence: Boolean?,
  @JsonProperty("perpetrator_domestic_violence")
  val isPerpetrator: Boolean?,
  @JsonProperty("use_of_alcohol")
  val alcoholUseIssues: ProblemsLevel?,
  @JsonProperty("binge_drinking")
  val bingeDrinkingIssues: ProblemsLevel?,
  @JsonProperty("impulsivity_issues")
  val impulsivityIssues: ProblemsLevel?,
  @JsonProperty("temper_control_issues")
  val temperControlIssues: ProblemsLevel?,
  @JsonProperty("pro_criminal_attitudes")
  val proCriminalAttitudes: ProblemsLevel?,
  @JsonProperty("previous_murder_attempt")
  val murderAttempt: Boolean?,
  @JsonProperty("previous_wounding")
  val wounding: Boolean?,
  @JsonProperty("previous_aggravated_burglary")
  val aggravatedBurglary: Boolean?,
  @JsonProperty("previous_arson")
  val arson: Boolean?,
  @JsonProperty("previous_criminal_damage")
  val criminalDamage: Boolean?,
  @JsonProperty("previous_kidnapping")
  val kidnapping: Boolean?,
  @JsonProperty("previous_possession_firearm")
  val firearmPossession: Boolean?,
  @JsonProperty("previous_robbery")
  val robbery: Boolean?,
  @JsonProperty("previous_offence_weapon")
  val offencesWithWeapon: Boolean?,
  @JsonProperty("current_possession_firearm")
  val currentFirearmPossession: Boolean?,
  @JsonProperty("current_offence_weapon")
  val currentOffencesWithWeapon: Boolean?
)
