package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

enum class SectionHeader(val value: String, val ordsUrlParam: String) {
  ROSH_SCREENING("ROSH", "riskindiv"),
  ROSH_FULL_ANALYSIS("ROSHFULL", "roshfull"),
  ROSH_SUMMARY("ROSHSUM", "roshsumm"),
}

enum class NeedsSection(val sectionNumber: Int, val description: String) {
  ACCOMMODATION(3, "Accommodation"),
  EDUCATION_TRAINING_AND_EMPLOYABILITY(4, "Education, Training and Employability"),
  RELATIONSHIPS(6, "Relationships"),
  LIFESTYLE_AND_ASSOCIATES(7, "Lifestyle and Associates"),
  DRUG_MISUSE(8, "Drug Misuse"),
  ALCOHOL_MISUSE(9, "Alcohol Misuse"),
  THINKING_AND_BEHAVIOUR(11, "Thinking and Behaviour"),
  ATTITUDE(12, "Attitudes"),
}
