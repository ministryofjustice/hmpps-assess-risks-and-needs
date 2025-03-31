package uk.gov.justice.digital.hmpps.assessrisksandneeds.integration

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AllRoshRiskDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentNeedDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentNeedsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.NeedSeverity
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OgpScoreDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OgrScoreDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OspScoreDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OtherRoshRisksDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OvpScoreDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ResponseDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskRoshSummaryDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskScoresDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RoshRiskToSelfDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrScoreDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrScoreSource
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.oasys.section.OasysThreshold
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.oasys.section.TierThreshold
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.AuditService
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.NeedsSection
import java.math.BigDecimal
import java.time.LocalDateTime

@AutoConfigureWebTestClient(timeout = "360000000")
@DisplayName("Assessment Tests")
class IntegrationControllerTest : IntegrationTestBase() {

  @MockkBean
  private lateinit var auditService: AuditService

  private val crn = "X123456"

  @BeforeEach
  fun setup() {
    every { auditService.sendEvent(any(), any()) } returns Unit
  }

  @Test
  fun `should return risk predictors for crn`() {
    webTestClient.get().uri("/risks/predictors/$crn")
      .headers(setAuthorisation(roles = listOf("ROLE_ARNS__RISKS__RO")))
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody<List<RiskScoresDto>>()
      .consumeWith {
        assertThat(it.responseBody).hasSize(3)
        assertThat(it.responseBody[0]).usingRecursiveComparison()
          .isEqualTo(
            RiskScoresDto(
              completedDate = LocalDateTime.of(2022, 6, 10, 18, 23, 20),
              assessmentStatus = "COMPLETE",
              groupReconvictionScore = OgrScoreDto(
                oneYear = BigDecimal.valueOf(3),
                twoYears = BigDecimal.valueOf(5),
                scoreLevel = ScoreLevel.LOW,
              ),
              violencePredictorScore = OvpScoreDto(
                ovpStaticWeightedScore = BigDecimal.valueOf(14),
                ovpDynamicWeightedScore = BigDecimal.valueOf(3),
                ovpTotalWeightedScore = BigDecimal.valueOf(17),
                oneYear = BigDecimal.valueOf(4),
                twoYears = BigDecimal.valueOf(7),
                ovpRisk = ScoreLevel.LOW,
              ),
              generalPredictorScore = OgpScoreDto(
                ogpStaticWeightedScore = BigDecimal.valueOf(3),
                ogpDynamicWeightedScore = BigDecimal.valueOf(7),
                ogpTotalWeightedScore = BigDecimal.valueOf(10),
                ogp1Year = BigDecimal.valueOf(4),
                ogp2Year = BigDecimal.valueOf(8),
                ogpRisk = ScoreLevel.LOW,
              ),
              riskOfSeriousRecidivismScore = RsrScoreDto(
                percentageScore = BigDecimal.valueOf(50.1234),
                staticOrDynamic = ScoreType.DYNAMIC,
                source = RsrScoreSource.OASYS,
                algorithmVersion = "11",
                ScoreLevel.MEDIUM,
              ),
              sexualPredictorScore = OspScoreDto(
                ospIndecentPercentageScore = BigDecimal.valueOf(2.81),
                ospContactPercentageScore = BigDecimal.valueOf(1.07),
                ospIndecentScoreLevel = ScoreLevel.MEDIUM,
                ospContactScoreLevel = ScoreLevel.MEDIUM,
              ),
            ),
          )
      }
  }

  @Test
  fun `should return rosh by crn`() {
    webTestClient.get().uri("/risks/rosh/$crn")
      .headers(setAuthorisation(roles = listOf("ROLE_ARNS__RISKS__RO")))
      .exchange()
      .expectStatus().isOk
      .expectBody<AllRoshRiskDto>()
      .consumeWith {
        assertThat(it.responseBody).isEqualTo(
          AllRoshRiskDto(
            RoshRiskToSelfDto(
              suicide = RiskDto(
                risk = ResponseDto.YES,
                previous = ResponseDto.YES,
                current = ResponseDto.YES,
                currentConcernsText = "Suicide and/or Self-harm current concerns",
              ),
              selfHarm = RiskDto(
                risk = ResponseDto.DK,
              ),
              custody = RiskDto(
                risk = ResponseDto.YES,
                previous = ResponseDto.YES,
                previousConcernsText = "Coping in custody / hostel setting previous concerns",
                current = ResponseDto.NA,
              ),
              hostelSetting = RiskDto(
                risk = ResponseDto.YES,
                previous = ResponseDto.DK,
                current = ResponseDto.NO,
              ),
              vulnerability = RiskDto(
                risk = ResponseDto.YES,
                previous = ResponseDto.YES,
                previousConcernsText = "Vulnerability previous concerns free text",
                current = ResponseDto.YES,
                currentConcernsText = "Vulnerability current concerns free text",
              ),
              assessedOn = null,
            ),
            OtherRoshRisksDto(
              ResponseDto.YES,
              ResponseDto.YES,
              ResponseDto.DK,
              ResponseDto.YES,
              assessedOn = null,
            ),
            RiskRoshSummaryDto(
              "whoisAtRisk",
              "natureOfRisk",
              "riskImminence",
              "riskIncreaseFactors",
              "riskMitigationFactors",
              mapOf(
                RiskLevel.LOW to listOf("Children", "Known Adult"),
                RiskLevel.MEDIUM to listOf("Public"),
                RiskLevel.HIGH to listOf("Staff"),
              ),
              mapOf(
                RiskLevel.LOW to listOf("Children", "Public", "Known Adult"),
                RiskLevel.HIGH to listOf("Prisoners"),
                RiskLevel.VERY_HIGH to listOf("Staff"),
              ),
              assessedOn = null,
            ),
            assessedOn = LocalDateTime.of(LocalDateTime.now().year - 1, 12, 19, 16, 57, 25),
          ),
        )
      }
  }

  @Test
  fun `get criminogenic needs by crn`() {
    val needsDto = webTestClient.get().uri("/needs/$crn")
      .headers(setAuthorisation(roles = listOf("ROLE_ARNS__RISKS__RO")))
      .exchange()
      .expectStatus().isOk
      .expectBody<AssessmentNeedsDto>()
      .returnResult().responseBody

    assertThat(needsDto?.assessedOn).isEqualTo(LocalDateTime.of(LocalDateTime.now().year - 1, 12, 19, 16, 57, 25))
    assertThat(needsDto?.identifiedNeeds).containsExactlyInAnyOrderElementsOf(identifiedNeeds())
    assertThat(needsDto?.notIdentifiedNeeds).containsExactlyInAnyOrderElementsOf(scoredNotNeeds())
  }

  @Test
  fun `get criminogenic needs returns not found`() {
    webTestClient.get().uri("/needs/NOT_FOUND")
      .headers(setAuthorisation(roles = listOf("ROLE_ARNS__RISKS__RO")))
      .exchange()
      .expectStatus().isNotFound
  }

  private fun scoredNotNeeds() = listOf(
    AssessmentNeedDto(
      section = NeedsSection.ACCOMMODATION.name,
      name = NeedsSection.ACCOMMODATION.description,
      riskOfHarm = false,
      riskOfReoffending = false,
      severity = NeedSeverity.NO_NEED,
      score = 0,
      oasysThreshold = OasysThreshold(2),
      tierThreshold = TierThreshold(2, 7),
    ),
    AssessmentNeedDto(
      section = NeedsSection.DRUG_MISUSE.name,
      name = NeedsSection.DRUG_MISUSE.description,
      severity = NeedSeverity.NO_NEED,
      score = 0,
      oasysThreshold = OasysThreshold(2),
      tierThreshold = TierThreshold(2, 8),
    ),
    AssessmentNeedDto(
      section = NeedsSection.ATTITUDE.name,
      name = NeedsSection.ATTITUDE.description,
      riskOfHarm = false,
      riskOfReoffending = false,
      severity = NeedSeverity.NO_NEED,
      score = 0,
      oasysThreshold = OasysThreshold(2),
      tierThreshold = TierThreshold(2, 7),
    ),
  )

  private fun identifiedNeeds() = listOf(
    AssessmentNeedDto(
      section = NeedsSection.EDUCATION_TRAINING_AND_EMPLOYABILITY.name,
      name = NeedsSection.EDUCATION_TRAINING_AND_EMPLOYABILITY.description,
      riskOfHarm = false,
      riskOfReoffending = false,
      severity = NeedSeverity.STANDARD,
      score = 3,
      oasysThreshold = OasysThreshold(3),
      tierThreshold = TierThreshold(3, 7),
    ),
    AssessmentNeedDto(
      section = NeedsSection.RELATIONSHIPS.name,
      name = NeedsSection.RELATIONSHIPS.description,
      riskOfHarm = false,
      riskOfReoffending = false,
      severity = NeedSeverity.STANDARD,
      score = 3,
      oasysThreshold = OasysThreshold(2),
      tierThreshold = TierThreshold(2, 5),
    ),
    AssessmentNeedDto(
      section = NeedsSection.LIFESTYLE_AND_ASSOCIATES.name,
      name = NeedsSection.LIFESTYLE_AND_ASSOCIATES.description,
      riskOfHarm = true,
      riskOfReoffending = true,
      severity = NeedSeverity.STANDARD,
      score = 3,
      oasysThreshold = OasysThreshold(2),
      tierThreshold = TierThreshold(2, 5),
    ),
    AssessmentNeedDto(
      section = NeedsSection.ALCOHOL_MISUSE.name,
      name = NeedsSection.ALCOHOL_MISUSE.description,
      riskOfHarm = false,
      riskOfReoffending = true,
      severity = NeedSeverity.STANDARD,
      score = 4,
      oasysThreshold = OasysThreshold(4),
      tierThreshold = TierThreshold(4, 7),
    ),
    AssessmentNeedDto(
      section = NeedsSection.THINKING_AND_BEHAVIOUR.name,
      name = NeedsSection.THINKING_AND_BEHAVIOUR.description,
      riskOfHarm = true,
      riskOfReoffending = true,
      severity = NeedSeverity.SEVERE,
      score = 7,
      oasysThreshold = OasysThreshold(4),
      tierThreshold = TierThreshold(4, 7),
    ),
  )
}
