package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api

import java.math.BigDecimal
import java.time.LocalDateTime

class OasysPredictorsDto(
  val oasysSetId: Long? = null,
  val refAssessmentVersionCode: String? = null,
  val refAssessmentVersionNumber: String? = null,
  val refAssessmentId: Long ? = null,
  val completedDate: LocalDateTime? = null,
  val voidedDateTime: LocalDateTime? = null,
  val assessmentCompleted: Boolean? = null,
  val assessmentStatus: String? = null,
  val rsr: RsrDto? = null,
)

class RsrDto(
  val rsrPercentageScore: BigDecimal? = null,
  val rsrStaticOrDynamic: String? = null,
  val rsrAlgorithmVersion: Long? = null,
  val rsrRiskRecon: RefElementDto? = null
)

data class RefElementDto(
  val code: String? = null,
  val shortDescription: String? = null,
  val description: String? = null
)
