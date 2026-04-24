package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

data class CriminogenicNeeds(
  val needScores: Map<AssessmentSection, NeedScores>,
)

data class NeedScores(
  val threshold: Int,
  val score: Int,
  val linkedToHarm: Boolean,
  val linkedToReoffending: Boolean,
)

enum class AssessmentSection {
  // All assessments
  ACCOMMODATION,
  LIFESTYLE_AND_ASSOCIATES,

  // OASys layer 3 assessment
  EDUCATION_TRAINING_AND_EMPLOYABILITY,
  RELATIONSHIPS,
  DRUG_MISUSE,
  ALCOHOL_MISUSE,
  THINKING_AND_BEHAVIOUR,
  ATTITUDE,

  // SAN assessment
  EMPLOYMENT_AND_EDUCATION,
  PERSONAL_RELATIONSHIPS_AND_COMMUNITY,
  DRUG_USE,
  ALCOHOL_USE,
  THINKING_ATTITUDES_AND_BEHAVIOUR,
}
