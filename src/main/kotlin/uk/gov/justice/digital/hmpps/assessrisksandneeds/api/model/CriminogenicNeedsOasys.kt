package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import java.time.LocalDateTime

data class CriminogenicNeedsOasys(
  val assessments: List<CriminogenicNeedsAssessmentOasys>,
)

data class CriminogenicNeedsAssessmentOasys(
  val assessmentVersion: String? = null,
  val dateCompleted: LocalDateTime? = null,
  val sanCrimNeedScore: SanNeeds? = null,
  val acc: AccNeeds? = null,
  val eTE: ETENeeds? = null,
  val rel: RelNeeds? = null,
  val lifecycle: LifecycleNeeds? = null,
  val drug: DrugNeeds? = null,
  val alcohol: AlcoholNeeds? = null,
  val think: ThinkNeeds? = null,
  val att: AttNeeds? = null,
)

data class SanNeeds(
  val accomSan: AccomSanNeeds? = null,
  val empAndEduSan: EmpAndEduSanNeeds? = null,
  val persRelAndCommSan: PersRelAndCommSanNeeds? = null,
  val lifeAndAssocSan: LifeAndAssocSanNeeds? = null,
  val drugUseSan: DrugUseSanNeeds? = null,
  val alcoUseSan: AlcoUseSanNeeds? = null,
  val thinkBehavAndAttiSan: ThinkBehavAndAttiSanNeeds? = null,
)

data class AccNeeds(
  val accThreshold: Int? = null,
  val accLinkedToHarm: String? = null,
  val accLinkedToReoffending: String? = null,
  val accOtherWeightedScore: Int? = null,
)

data class ETENeeds(
  val eTEThreshold: Int? = null,
  val eTELinkedToHarm: String? = null,
  val eTELinkedToReoffending: String? = null,
  val eTEOtherWeightedScore: Int? = null,
)

data class RelNeeds(
  val relThreshold: Int? = null,
  val relLinkedToHarm: String? = null,
  val relLinkedToReoffending: String? = null,
  val relOtherWeightedScore: Int? = null,
)

data class LifecycleNeeds(
  val lifecycleThreshold: Int? = null,
  val lifecycleLinkedToHarm: String? = null,
  val lifecycleLinkedToReoffending: String? = null,
  val lifecycleOtherWeightedScore: Int? = null,
)

data class DrugNeeds(
  val drugThreshold: Int? = null,
  val drugLinkedToHarm: String? = null,
  val drugLinkedToReoffending: String? = null,
  val drugOtherWeightedScore: Int? = null,
)

data class AlcoholNeeds(
  val alcoholThreshold: Int? = null,
  val alcoholLinkedToHarm: String? = null,
  val alcoholLinkedToReoffending: String? = null,
  val alcoholOtherWeightedScore: Int? = null,
)

data class ThinkNeeds(
  val thinkThreshold: Int? = null,
  val thinkLinkedToHarm: String? = null,
  val thinkLinkedToReoffending: String? = null,
  val thinkOtherWeightedScore: Int? = null,
)

data class AttNeeds(
  val attThreshold: Int? = null,
  val attLinkedToHarm: String? = null,
  val attLinkedToReoffending: String? = null,
  val attOtherWeightedScore: Int? = null,
)

data class AccomSanNeeds(
  val accomSanThreshold: Int? = null,
  val accomSanLinkedToHarm: String? = null,
  val accomSanLinkedToReoffending: String? = null,
  val accomSanScore: Int? = null,
)

data class EmpAndEduSanNeeds(
  val empAndEduSanThreshold: Int? = null,
  val empAndEduSanLinkedToHarm: String? = null,
  val empAndEduSanLinkedToReoffending: String? = null,
  val empAndEduSanScore: Int? = null,
)

data class PersRelAndCommSanNeeds(
  val persRelAndCommSanThreshold: Int? = null,
  val persRelAndCommSanLinkedToHarm: String? = null,
  val persRelAndCommSanLinkedToReoffending: String? = null,
  val persRelAndCommSanScore: Int? = null,
)

data class LifeAndAssocSanNeeds(
  val lifeAndAssocSanThreshold: Int? = null,
  val lifeAndAssocSanLinkedToHarm: String? = null,
  val lifeAndAssocSanLinkedToReoffending: String? = null,
  val lifeAndAssocSanScore: Int? = null,
)

data class DrugUseSanNeeds(
  val drugUseSanThreshold: Int? = null,
  val drugUseSanLinkedToHarm: String? = null,
  val drugUseSanLinkedToReoffending: String? = null,
  val drugUseSanScore: Int? = null,
)

data class AlcoUseSanNeeds(
  val alcoholUseSanThreshold: Int? = null,
  val alcoholUseSanLinkedToHarm: String? = null,
  val alcoholUseSanLinkedToReoffending: String? = null,
  val alcoholUseSanScore: Int? = null,
)

data class ThinkBehavAndAttiSanNeeds(
  val thinkBehavAndAttiSanThreshold: Int? = null,
  val thinkBehavAndAttiSanLinkedToHarm: String? = null,
  val thinkBehavAndAttiSanLinkedToReoffending: String? = null,
  val thinkBehavAndAttiSanScore: Int? = null,
)
