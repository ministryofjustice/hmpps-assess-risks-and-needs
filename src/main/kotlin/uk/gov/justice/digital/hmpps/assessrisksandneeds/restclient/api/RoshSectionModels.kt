package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api

import com.fasterxml.jackson.annotation.JsonAlias
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OtherRoshRisksDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ResponseDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskLevel.Companion.fromString
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskRoshSummaryDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RoshRiskToSelfDto
import java.time.LocalDateTime
import java.util.Comparator.comparing

data class RoshScreening(
  val concernsRiskOfSuicide: String? = null,
  val concernsRiskOfSelfHarm: String? = null,
  val concernsCopingInCustody: String? = null,
  val concernsVulnerability: String? = null,
)

data class RoshFull(
  val dateCompleted: LocalDateTime? = null,
  val currentConcernsRiskOfSelfHarm: String? = null,
  val pastSelfHarmConcerns: String? = null,
  val currentConcernsRiskOfSuicide: String? = null,
  val pastSuicideConcerns: String? = null,
  val currentConcernsSelfHarmSuicide: String? = null,
  val previousConcernsSelfHarmSuicide: String? = null,
  val currentConcernsCustody: String? = null,
  val previousConcernsCustodyCoping: String? = null,
  val currentConcernsHostel: String? = null,
  val previousConcernsHostelCoping: String? = null,
  val currentCustodyHostelCoping: String? = null,
  val previousCustodyHostelCoping: String? = null,
  @JsonAlias("currentConcernsVulnerablity") // Typo in ords endpoint
  val currentConcernsVulnerability: String? = null,
  val previousConcernsVulnerability: String? = null,
  val currentVulnerability: String? = null,
  val previousVulnerability: String? = null,
  val currentConcernsEscape: String? = null,
  val currentConcernsDisruptive: String? = null,
  val currentConcernsBreachOfTrust: String? = null,
  val tickRiskOfSeriousHarm: String? = null,
) {
  fun asRiskToSelf(roshScreening: RoshScreening) = RoshRiskToSelfDto(
    riskOfSuicide(roshScreening.concernsRiskOfSuicide),
    riskOfSelfHarm(roshScreening.concernsRiskOfSelfHarm),
    riskInCustody(roshScreening.concernsCopingInCustody),
    riskInHostel(roshScreening.concernsCopingInCustody),
    vulnerability(roshScreening.concernsCopingInCustody),
    dateCompleted,
  )

  fun asOtherRisks() = OtherRoshRisksDto(
    ResponseDto.fromString(currentConcernsEscape),
    ResponseDto.fromString(currentConcernsDisruptive),
    ResponseDto.fromString(currentConcernsBreachOfTrust),
    ResponseDto.fromString(tickRiskOfSeriousHarm),
    dateCompleted,
  )

  private fun riskOfSuicide(screening: String?) = risk(
    screening,
    currentConcernsRiskOfSuicide,
    pastSuicideConcerns,
    currentConcernsSelfHarmSuicide,
    previousConcernsSelfHarmSuicide,
  )

  private fun riskOfSelfHarm(screening: String?) = risk(
    screening,
    currentConcernsRiskOfSelfHarm,
    pastSelfHarmConcerns,
    currentConcernsSelfHarmSuicide,
    previousConcernsSelfHarmSuicide,
  )

  private fun ResponseDto?.conditionalText(text: String? = null) = when (this) {
    ResponseDto.YES -> text
    else -> null
  }

  private fun riskInCustody(screening: String?) = risk(
    screening,
    currentConcernsCustody,
    previousConcernsCustodyCoping,
    currentCustodyHostelCoping,
    previousCustodyHostelCoping,
  )

  private fun riskInHostel(screening: String?) = risk(
    screening,
    currentConcernsHostel,
    previousConcernsHostelCoping,
    currentCustodyHostelCoping,
    previousCustodyHostelCoping,
  )

  private fun vulnerability(screening: String?) = risk(
    screening,
    currentConcernsVulnerability,
    previousConcernsVulnerability,
    currentVulnerability,
    previousVulnerability,
  )

  private fun risk(
    screening: String?,
    current: String?,
    previous: String?,
    currentText: String?,
    previousText: String?,
  ): RiskDto {
    val cur = ResponseDto.fromString(current)
    val prev = ResponseDto.fromString(previous)
    return RiskDto(
      risk = ResponseDto.fromString(screening),
      current = cur,
      previous = prev,
      currentConcernsText = cur.conditionalText(currentText),
      previousConcernsText = prev.conditionalText(previousText),
    )
  }
}

data class RoshSummary(
  val dateCompleted: LocalDateTime? = null,
  private val riskPrisonersCustody: String? = null,
  private val riskStaffCustody: String? = null,
  private val riskStaffCommunity: String? = null,
  private val riskKnownAdultCustody: String? = null,
  private val riskKnownAdultCommunity: String? = null,
  private val riskPublicCustody: String? = null,
  private val riskPublicCommunity: String? = null,
  private val riskChildrenCustody: String? = null,
  private val riskChildrenCommunity: String? = null,
  val whoAtRisk: String? = null,
  val factorsLikelyToReduceRisk: String? = null,
  val factorsLikelyToIncreaseRisk: String? = null,
  val riskGreatest: String? = null,
  val natureOfRisk: String? = null,
) {
  val prisonersCustody: RiskLevel? = fromString(riskPrisonersCustody)
  val staffCustody: RiskLevel? = fromString(riskStaffCustody)
  val staffCommunity: RiskLevel? = fromString(riskStaffCommunity)
  val knownAdultCustody: RiskLevel? = fromString(riskKnownAdultCustody)
  val knownAdultCommunity: RiskLevel? = fromString(riskKnownAdultCommunity)
  val publicCustody: RiskLevel? = fromString(riskPublicCustody)
  val publicCommunity: RiskLevel? = fromString(riskPublicCommunity)
  val childrenCustody: RiskLevel? = fromString(riskChildrenCustody)
  val childrenCommunity: RiskLevel? = fromString(riskChildrenCommunity)

  fun asRiskRoshSummary() = RiskRoshSummaryDto(
    whoAtRisk,
    natureOfRisk,
    riskGreatest,
    factorsLikelyToIncreaseRisk,
    factorsLikelyToReduceRisk,
    listOf(
      CHILDREN to childrenCommunity,
      PUBLIC to publicCommunity,
      KNOWN_ADULT to knownAdultCommunity,
      STAFF to staffCommunity,
    ).asRiskLevelMap(),
    listOf(
      CHILDREN to childrenCustody,
      PUBLIC to publicCustody,
      KNOWN_ADULT to knownAdultCustody,
      STAFF to staffCustody,
      PRISONERS to prisonersCustody,
    ).asRiskLevelMap(),
    dateCompleted,
  )

  private fun List<Pair<String, RiskLevel?>>.asRiskLevelMap() =
    filter { it.second != null }
      .groupBy({ it.second }, { it.first })
      .toSortedMap(comparing(RiskLevel::ordinal).reversed())

  companion object {
    val CHILDREN = "Children"
    val PUBLIC = "Public"
    val KNOWN_ADULT = "Known Adult"
    val STAFF = "Staff"
    val PRISONERS = "Prisoners"
  }
}

data class RoshContainer<T>(val assessments: List<T>)
