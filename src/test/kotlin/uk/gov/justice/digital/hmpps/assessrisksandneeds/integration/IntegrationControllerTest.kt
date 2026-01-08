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
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AllPredictorVersioned
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AllPredictorVersionedDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AllPredictorVersionedLegacyDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AllRoshRiskDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentNeedDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentNeedsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentStatus
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.NeedSeverity
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OgpScoreDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OgrScoreDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OspScoreDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OtherRoshRisksDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OvpScoreDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ResponseDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskManagementPlansDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskRoshSummaryDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskScoresDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RoshRiskToSelfDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrScoreDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrScoreSource
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ogrs4.AllPredictorDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ogrs4.BasePredictorDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ogrs4.StaticOrDynamicPredictorDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ogrs4.VersionedStaticOrDynamicPredictorDto
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
  fun `get risk predictors for crn`() {
    webTestClient.get().uri("/risks/predictors/$crn")
      .headers(setAuthorisation(roles = listOf("ROLE_ARNS__RISKS__RO")))
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody<List<RiskScoresDto>>()
      .consumeWith {
        assertThat(it.responseBody).hasSize(5)
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
                algorithmVersion = "5",
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
  fun `get rosh by crn`() {
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
            assessedOn = LocalDateTime.of(LocalDateTime.now().year - 1, LocalDateTime.now().monthValue, 19, 16, 57, 25),
          ),
        )
      }
  }

  @Test
  fun `get rosh by crn within timeframe`() {
    val timeframe = 60L
    webTestClient.get().uri("/risks/rosh/$crn/$timeframe")
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
            assessedOn = LocalDateTime.of(LocalDateTime.now().year - 1, LocalDateTime.now().monthValue, 19, 16, 57, 25),
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

    assertThat(needsDto?.assessedOn).isEqualTo(LocalDateTime.of(LocalDateTime.now().year - 1, LocalDateTime.now().monthValue, 19, 16, 57, 25))
    assertThat(needsDto?.identifiedNeeds).containsExactlyInAnyOrderElementsOf(identifiedNeeds())
    assertThat(needsDto?.notIdentifiedNeeds).containsExactlyInAnyOrderElementsOf(scoredNotNeeds())
  }

  @Test
  fun `get criminogenic needs by crn for an incomplete assessment`() {
    val needsDto = webTestClient.get().uri("/needs/$crn?excludeIncomplete=false")
      .headers(setAuthorisation(roles = listOf("ROLE_ARNS__RISKS__RO")))
      .exchange()
      .expectStatus().isOk
      .expectBody<AssessmentNeedsDto>()
      .returnResult().responseBody

    assertThat(needsDto?.assessedOn).isNull()
    assertThat(needsDto?.identifiedNeeds).containsExactlyInAnyOrderElementsOf(identifiedNeeds())
    assertThat(needsDto?.notIdentifiedNeeds).containsExactlyInAnyOrderElementsOf(scoredNotNeeds())
  }

  @Test
  fun `get criminogenic needs by crn within timeframe`() {
    val timeframe = 60L
    val needsDto = webTestClient.get().uri("/needs/$crn/$timeframe")
      .headers(setAuthorisation(roles = listOf("ROLE_ARNS__RISKS__RO")))
      .exchange()
      .expectStatus().isOk
      .expectBody<AssessmentNeedsDto>()
      .returnResult().responseBody

    assertThat(needsDto?.assessedOn).isEqualTo(LocalDateTime.of(LocalDateTime.now().year - 1, LocalDateTime.now().monthValue, 19, 16, 57, 25))
    assertThat(needsDto?.identifiedNeeds).containsExactlyInAnyOrderElementsOf(identifiedNeeds())
    assertThat(needsDto?.notIdentifiedNeeds).containsExactlyInAnyOrderElementsOf(scoredNotNeeds())
  }

  @Test
  fun `get criminogenic needs by crn within timeframe not found`() {
    val timeframe = 2L
    val needsDto = webTestClient.get().uri("/needs/$crn/$timeframe")
      .headers(setAuthorisation(roles = listOf("ROLE_ARNS__RISKS__RO")))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `get risk management plans by crn`() {
    val riskManagementPlanDetails = webTestClient.get().uri("/risks/risk-management-plan/$crn")
      .headers(setAuthorisation(roles = listOf("ROLE_ARNS__RISKS__RO")))
      .exchange()
      .expectStatus().isOk
      .expectBody<RiskManagementPlansDto>()
      .returnResult().responseBody

    assertThat(riskManagementPlanDetails!!.riskManagementPlan).hasSize(5)
    with(riskManagementPlanDetails.riskManagementPlan[0]) {
      assertThat(this.assessmentId).isEqualTo(667025L)
      assertThat(this.initiationDate).isEqualTo(LocalDateTime.of(2020, 3, 26, 12, 38, 57))
      assertThat(this.dateCompleted).isEqualTo(LocalDateTime.of(2020, 3, 26, 12, 47, 17))
      assertThat(this.assessmentStatus).isEqualTo("COMPLETE")
      assertThat(this.assessmentType).isEqualTo("LAYER3")
      assertThat(this.partcompStatus).isNull()
      assertThat(this.keyInformationCurrentSituation).isEqualTo(null)
      assertThat(this.furtherConsiderationsCurrentSituation).isEqualTo(null)
      assertThat(this.supervision).isEqualTo(null)
      assertThat(this.monitoringAndControl).isEqualTo(null)
      assertThat(this.interventionsAndTreatment).isEqualTo(null)
      assertThat(this.victimSafetyPlanning).isEqualTo(null)
      assertThat(this.contingencyPlans).isEqualTo(null)
    }
    with(riskManagementPlanDetails.riskManagementPlan[3]) {
      assertThat(this.assessmentId).isEqualTo(674025L)
      assertThat(this.initiationDate).isEqualTo(LocalDateTime.of(2020, 6, 25, 13, 4, 56))
      assertThat(this.dateCompleted).isEqualTo(LocalDateTime.of(2020, 11, 2, 14, 49, 39))
      assertThat(this.assessmentStatus).isEqualTo("LOCKED_INCOMPLETE")
      assertThat(this.assessmentType).isEqualTo("LAYER3")
      assertThat(this.partcompStatus).isEqualTo("Unsigned")
      assertThat(this.keyInformationCurrentSituation).isEqualTo(null)
      assertThat(this.furtherConsiderationsCurrentSituation).isEqualTo(null)
      assertThat(this.supervision).isEqualTo(null)
      assertThat(this.monitoringAndControl).isEqualTo(null)
      assertThat(this.interventionsAndTreatment).isEqualTo(null)
      assertThat(this.victimSafetyPlanning).isEqualTo(null)
      assertThat(this.contingencyPlans).isEqualTo(null)
    }
    with(riskManagementPlanDetails.riskManagementPlan[4]) {
      assertThat(this.assessmentId).isEqualTo(676026L)
      assertThat(this.initiationDate).isEqualTo(LocalDateTime.of(2020, 11, 2, 14, 50, 2))
      assertThat(this.dateCompleted).isEqualTo(LocalDateTime.of(2020, 11, 5, 10, 56, 37))
      assertThat(this.assessmentStatus).isEqualTo("COMPLETE")
      assertThat(this.assessmentType).isEqualTo("LAYER3")
      assertThat(this.keyInformationCurrentSituation).isEqualTo("Key considerations")
      assertThat(this.furtherConsiderationsCurrentSituation).isEqualTo("Kelvin Brown is currently in the community having received a Adjourned - Other Report on the 01/01/2010 for 12 months\r\rThe end of their sentence is currently unknown. \r\rThey have no areas linked to harm. \r\rKelvin Brown has been assessed as medium risk to the public.\r\rKelvin Brown will have contact with a child on the protection register or in local authority care.\rThey are quite motivated to address offending behaviour.")
      assertThat(this.supervision).isEqualTo(null)
      assertThat(this.monitoringAndControl).isEqualTo("3. Added measures for specific risks. Include here all activity aimed at addressing victim perspective and contact.")
      assertThat(this.interventionsAndTreatment).isEqualTo("5. Additional conditions/requirements to manage the specific risks.")
      assertThat(this.victimSafetyPlanning).isEqualTo("7. Contingency")
      assertThat(this.contingencyPlans).isEqualTo(null)
      assertThat(this.laterWIPAssessmentExists).isEqualTo(false)
      assertThat(this.latestWIPDate).isEqualTo(LocalDateTime.of(2022, 7, 21, 15, 43, 58))
      assertThat(this.laterSignLockAssessmentExists).isEqualTo(false)
      assertThat(this.latestSignLockDate).isNull()
      assertThat(this.laterPartCompUnsignedAssessmentExists).isEqualTo(false)
      assertThat(this.latestPartCompUnsignedDate).isEqualTo(LocalDateTime.of(2022, 5, 31, 10, 37, 5))
      assertThat(this.laterPartCompSignedAssessmentExists).isEqualTo(false)
      assertThat(this.latestPartCompSignedDate).isNull()
      assertThat(this.laterCompleteAssessmentExists).isEqualTo(false)
      assertThat(this.latestCompleteDate).isEqualTo(LocalDateTime.of(2022, 7, 21, 15, 43, 12))
    }
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

  @Test
  fun `should return versioned risk data for valid crn`() {
    // Given
    val identifierType = "crn"
    val identifierValue = "X123456"

    // When
    webTestClient.get()
      .uri("/risks/predictors/unsafe/all/$identifierType/$identifierValue")
      .header("Content-Type", "application/json")
      .headers(setAuthorisation(user = "assess-risks-needs", roles = listOf("ROLE_ARNS__RISKS__RO")))
      .exchange()
      // Then
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody<List<AllPredictorVersioned<Any>>>()
      .consumeWith {
        assertThat(it.responseBody).hasSize(5)
        assertThat(it.responseBody!![0]).usingRecursiveComparison()
          .isEqualTo(
            AllPredictorVersionedLegacyDto(
              completedDate = LocalDateTime.of(2022, 6, 10, 18, 23, 20),
              status = AssessmentStatus.COMPLETE,
              outputVersion = "1",
              output = RiskScoresDto(
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
                  algorithmVersion = "5",
                  ScoreLevel.MEDIUM,
                ),
                sexualPredictorScore = OspScoreDto(
                  ospIndecentPercentageScore = BigDecimal.valueOf(2.81),
                  ospContactPercentageScore = BigDecimal.valueOf(1.07),
                  ospIndecentScoreLevel = ScoreLevel.MEDIUM,
                  ospContactScoreLevel = ScoreLevel.MEDIUM,
                ),
              ),
            ),
          )
        assertThat(it.responseBody!![4]).usingRecursiveComparison()
          .isEqualTo(
            AllPredictorVersionedDto(
              completedDate = LocalDateTime.of(2022, 6, 12, 18, 23, 20),
              status = AssessmentStatus.COMPLETE,
              outputVersion = "2",
              output = AllPredictorDto(
                allReoffendingPredictor = StaticOrDynamicPredictorDto(
                  staticOrDynamic = ScoreType.STATIC,
                  score = BigDecimal.valueOf(1.23),
                  band = ScoreLevel.LOW,
                ),
                violentReoffendingPredictor = StaticOrDynamicPredictorDto(
                  staticOrDynamic = ScoreType.STATIC,
                  score = BigDecimal.valueOf(1.23),
                  band = ScoreLevel.LOW,
                ),
                seriousViolentReoffendingPredictor = StaticOrDynamicPredictorDto(
                  staticOrDynamic = ScoreType.STATIC,
                  score = BigDecimal.valueOf(1.23),
                  band = ScoreLevel.LOW,
                ),
                directContactSexualReoffendingPredictor = BasePredictorDto(
                  score = BigDecimal.valueOf(2.81),
                  band = ScoreLevel.MEDIUM,
                ),
                indirectImageContactSexualReoffendingPredictor = BasePredictorDto(
                  score = BigDecimal.valueOf(1.07),
                  band = ScoreLevel.MEDIUM,
                ),
                combinedSeriousReoffendingPredictor = VersionedStaticOrDynamicPredictorDto(
                  algorithmVersion = "6",
                  staticOrDynamic = ScoreType.STATIC,
                  score = BigDecimal.valueOf(1.23),
                  band = ScoreLevel.LOW,
                ),
              ),
            ),
          )
      }
  }

  @Test
  fun `should return not found error for invalid crn for versioned risk scores`() {
    webTestClient.get().uri("/risks/predictors/unsafe/all/crn/X999999")
      .headers(setAuthorisation(roles = listOf("ROLE_ARNS__RISKS__RO")))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `should return 400 bad request for invalid identifier type for versioned risk scores`() {
    val identifierType = "INVALID_IDENTIFIER_TYPE"
    val identifierValue = "X234567"
    webTestClient.get().uri("/risks/predictors/unsafe/all/$identifierType/$identifierValue")
      .headers(setAuthorisation(roles = listOf("ROLE_ARNS__RISKS__RO")))
      .exchange()
      .expectStatus().isBadRequest
  }
}
