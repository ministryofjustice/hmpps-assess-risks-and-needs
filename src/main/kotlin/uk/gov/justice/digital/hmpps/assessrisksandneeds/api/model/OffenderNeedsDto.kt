package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import java.time.LocalDateTime

data class OffenderNeedsDto(
  val needs: Collection<OffenderNeedDto>,
  val assessedOn: LocalDateTime,
  val historicStatus: String
)

data class OffenderNeedDto(
  val section: String? = null,
  val name: String? = null,
  val overThreshold: Boolean? = null,
  val riskOfHarm: Boolean? = null,
  val riskOfReoffending: Boolean? = null,
  val flaggedAsNeed: Boolean? = null,
  val severity: NeedSeverity? = null,
  val identifiedAsNeed: Boolean? = null,
  val needScore: Long? = null
)
