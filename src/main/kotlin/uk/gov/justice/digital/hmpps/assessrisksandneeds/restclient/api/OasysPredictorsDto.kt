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
  val ogr3: Ogrs3Dto? = null,
  val ovp: OvpDto? = null,
  val ogp: OgpDto? = null,
  val rsr: RsrDto? = null,
  val osp: OspDto? = null

) {
  fun hasRsrScores(): Boolean {
    return rsr?.rsrPercentageScore != null &&
      rsr.rsrStaticOrDynamic != null &&
      rsr.rsrAlgorithmVersion != null &&
      rsr.rsrRiskRecon != null
  }
}

class Ogrs3Dto(
  val ogrs3_1Year: BigDecimal? = null,
  val ogrs3_2Year: BigDecimal? = null,
  val reconvictionRisk: RefElementDto? = null
)

class OvpDto(
  val ovpStaticWeightedScore: BigDecimal? = null,
  val ovpDynamicWeightedScore: BigDecimal? = null,
  val ovpTotalWeightedScore: BigDecimal? = null,
  val ovp1Year: BigDecimal? = null,
  val ovp2Year: BigDecimal? = null,
  val ovpRisk: RefElementDto? = null,
  val ovpPreviousWeightedScore: BigDecimal? = null,
  val ovpViolentWeightedScore: BigDecimal? = null,
  val ovpNonViolentWeightedScore: BigDecimal? = null,
  val ovpAgeWeightedScore: BigDecimal? = null,
  val ovpSexWeightedScore: BigDecimal? = null
)

class OgpDto(
  val ogpStaticWeightedScore: BigDecimal? = null,
  val ogpDynamicWeightedScore: BigDecimal? = null,
  val ogpTotalWeightedScore: BigDecimal? = null,
  val ogp1Year: BigDecimal? = null,
  val ogp2Year: BigDecimal? = null,
  val ogpRisk: RefElementDto? = null
)

class RsrDto(
  val rsrPercentageScore: BigDecimal? = null,
  val rsrStaticOrDynamic: String? = null,
  val rsrAlgorithmVersion: Long? = null,
  val rsrRiskRecon: RefElementDto? = null
)

data class OspDto(
  val ospIndecentPercentageScore: BigDecimal? = null,
  val ospContactPercentageScore: BigDecimal? = null,
  val ospIndecentRiskRecon: RefElementDto? = null,
  val ospContactRiskRecon: RefElementDto? = null
)

data class RefElementDto(
  val code: String? = null,
  val shortDescription: String? = null,
  val description: String? = null
)
