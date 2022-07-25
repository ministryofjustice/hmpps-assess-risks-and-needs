package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import java.time.LocalDateTime

abstract class CommonAssessmentDto {
  open val laterWIPAssessmentExists: Boolean? = null
  open val latestWIPDate: LocalDateTime? = null
  open val laterSignLockAssessmentExists: Boolean? = null
  open val latestSignLockDate: LocalDateTime? = null
  open val laterPartCompUnsignedAssessmentExists: Boolean? = null
  open val latestPartCompUnsignedDate: LocalDateTime? = null
  open val laterPartCompSignedAssessmentExists: Boolean? = null
  open val latestPartCompSignedDate: LocalDateTime? = null
  open val laterCompleteAssessmentExists: Boolean? = null
  open val latestCompleteDate: LocalDateTime? = null
}
