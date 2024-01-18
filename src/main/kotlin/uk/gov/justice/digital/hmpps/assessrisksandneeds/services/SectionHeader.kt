package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

enum class SectionHeader(val value: String, val ordsUrlParam: String) {
  ROSH_SCREENING("ROSH", "rosh"),
  ROSH_FULL_ANALYSIS("ROSHFULL", "roshfull"),
  ROSH_SUMMARY("ROSHSUM", "roshsumm"),
}

enum class NeedsSection(val sectionNumber: Int) {
  ACCOMMODATION(3),
  EDUCATION_TRAINING_EMPLOYMENT(4),
  RELATIONSHIPS(6),
  LIFESTYLE(7),
  DRUG_MISUSE(8),
  ALCOHOL_MISUSE(9),
  THINKING_AND_BEHAVIOUR(11),
  ATTITUDE(12),
}
