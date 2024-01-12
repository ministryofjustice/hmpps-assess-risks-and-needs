package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

enum class SectionHeader(val value: String, val ordsUrlParam: String) {
  ROSH_SCREENING("ROSH", "rosh"),
  ROSH_FULL_ANALYSIS("ROSHFULL", "roshfull"),
  ROSH_SUMMARY("ROSHSUM", "roshsumm"),
}
