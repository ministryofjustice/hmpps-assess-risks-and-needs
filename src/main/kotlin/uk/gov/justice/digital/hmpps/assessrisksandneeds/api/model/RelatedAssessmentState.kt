package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import java.time.LocalDateTime

interface RelatedAssessmentState {
  val laterWIPAssessmentExists: Boolean?
  val latestWIPDate: LocalDateTime?
  val laterSignLockAssessmentExists: Boolean?
  val latestSignLockDate: LocalDateTime?
  val laterPartCompUnsignedAssessmentExists: Boolean?
  val latestPartCompUnsignedDate: LocalDateTime?
  val laterPartCompSignedAssessmentExists: Boolean?
  val latestPartCompSignedDate: LocalDateTime?
  val laterCompleteAssessmentExists: Boolean?
  val latestCompleteDate: LocalDateTime?
}
