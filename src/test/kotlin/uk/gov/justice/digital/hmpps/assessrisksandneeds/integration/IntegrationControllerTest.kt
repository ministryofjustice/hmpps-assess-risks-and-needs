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
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentNeedDetailDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentNeedsDetailsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentSection
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentStatus
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentVersion
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.NeedStatus
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
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.AuditService
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
        assertThat(it?.responseBody?.get(0)).usingRecursiveComparison()
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
              "analysisOfRiskFactors",
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
            assessedOn = LocalDateTime.of(2024, 12, 19, 16, 57, 25),
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
              "analysisOfRiskFactors",
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
            assessedOn = LocalDateTime.of(2024, 12, 19, 16, 57, 25),
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
      .expectBody<AssessmentNeedsDetailsDto>()
      .returnResult().responseBody

    assertThat(needsDto?.assessmentVersion).isEqualTo(AssessmentVersion.OASYS)
    assertThat(needsDto?.assessedOn).isEqualTo(LocalDateTime.of(2024, 12, 19, 16, 57, 25))
    assertThat(needsDto?.needs).containsExactlyElementsOf(oasysNeedDetails())
  }

  @Test
  fun `get criminogenic needs by crn for an incomplete assessment`() {
    val needsDto = webTestClient.get().uri("/needs/$crn?excludeIncomplete=false")
      .headers(setAuthorisation(roles = listOf("ROLE_ARNS__RISKS__RO")))
      .exchange()
      .expectStatus().isOk
      .expectBody<AssessmentNeedsDetailsDto>()
      .returnResult().responseBody

    assertThat(needsDto?.assessmentVersion).isEqualTo(AssessmentVersion.OASYS)
    assertThat(needsDto?.assessedOn).isNull()
    assertThat(needsDto?.needs).containsExactlyElementsOf(oasysNeedDetails())
  }

  @Test
  fun `get criminogenic needs by crn within timeframe`() {
    val timeframe = 60L
    val needsDto = webTestClient.get().uri("/needs/$crn/$timeframe")
      .headers(setAuthorisation(roles = listOf("ROLE_ARNS__RISKS__RO")))
      .exchange()
      .expectStatus().isOk
      .expectBody<AssessmentNeedsDetailsDto>()
      .returnResult().responseBody

    assertThat(needsDto?.assessmentVersion).isEqualTo(AssessmentVersion.OASYS)
    assertThat(needsDto?.assessedOn).isEqualTo(LocalDateTime.of(2024, 12, 19, 16, 57, 25))
    assertThat(needsDto?.needs).containsExactlyElementsOf(oasysNeedDetails())
  }

  @Test
  fun `get criminogenic needs by crn within timeframe not found`() {
    val timeframe = 2L
    webTestClient.get().uri("/needs/$crn/$timeframe")
      .headers(setAuthorisation(roles = listOf("ROLE_ARNS__RISKS__RO")))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `get criminogenic needs by crn for a SAN assessment`() {
    val needsDto = webTestClient.get().uri("/needs/X654321")
      .headers(setAuthorisation(roles = listOf("ROLE_ARNS__RISKS__RO")))
      .exchange()
      .expectStatus().isOk
      .expectBody<AssessmentNeedsDetailsDto>()
      .returnResult().responseBody

    assertThat(needsDto?.assessmentVersion).isEqualTo(AssessmentVersion.SAN)
    assertThat(needsDto?.assessedOn).isEqualTo(LocalDateTime.of(2024, 12, 20, 10, 0, 0))
    assertThat(needsDto?.needs).containsExactlyElementsOf(sanNeedDetails())
    assertThat(needsDto?.identifiedNeeds).containsExactlyInAnyOrderElementsOf(sanIdentifiedNeeds())
    assertThat(needsDto?.notIdentifiedNeeds).containsExactlyInAnyOrderElementsOf(sanNotIdentifiedNeeds())
    assertThat(needsDto?.unansweredNeeds).isEmpty()
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

  // Detailed needs are returned as a single list ordered by need status (identified, not-identified, unanswered,
  // unscored) and, within each status, in canonical section order.
  private fun oasysNeedDetails() = listOf(
    AssessmentNeedDetailDto(
      section = AssessmentSection.EDUCATION_TRAINING_AND_EMPLOYABILITY.name,
      name = "Education, Training and Employability",
      needStatus = NeedStatus.IDENTIFIED_NEED,
      riskOfHarm = false,
      riskOfReoffending = false,
      score = 3,
      oasysThreshold = OasysThreshold(3),
    ),
    AssessmentNeedDetailDto(
      section = AssessmentSection.RELATIONSHIPS.name,
      name = "Relationships",
      needStatus = NeedStatus.IDENTIFIED_NEED,
      riskOfHarm = false,
      riskOfReoffending = false,
      score = 3,
      oasysThreshold = OasysThreshold(2),
    ),
    AssessmentNeedDetailDto(
      section = AssessmentSection.LIFESTYLE_AND_ASSOCIATES.name,
      name = "Lifestyle and Associates",
      needStatus = NeedStatus.IDENTIFIED_NEED,
      riskOfHarm = true,
      riskOfReoffending = true,
      score = 3,
      oasysThreshold = OasysThreshold(2),
    ),
    AssessmentNeedDetailDto(
      section = AssessmentSection.ALCOHOL_MISUSE.name,
      name = "Alcohol Misuse",
      needStatus = NeedStatus.IDENTIFIED_NEED,
      riskOfHarm = false,
      riskOfReoffending = true,
      score = 4,
      oasysThreshold = OasysThreshold(4),
    ),
    AssessmentNeedDetailDto(
      section = AssessmentSection.THINKING_AND_BEHAVIOUR.name,
      name = "Thinking and Behaviour",
      needStatus = NeedStatus.IDENTIFIED_NEED,
      riskOfHarm = true,
      riskOfReoffending = true,
      score = 7,
      oasysThreshold = OasysThreshold(4),
    ),
    AssessmentNeedDetailDto(
      section = AssessmentSection.ACCOMMODATION.name,
      name = "Accommodation",
      needStatus = NeedStatus.NOT_IDENTIFIED_NEED,
      riskOfHarm = false,
      riskOfReoffending = false,
      score = 0,
      oasysThreshold = OasysThreshold(2),
    ),
    AssessmentNeedDetailDto(
      section = AssessmentSection.DRUG_MISUSE.name,
      name = "Drug Misuse",
      needStatus = NeedStatus.NOT_IDENTIFIED_NEED,
      riskOfHarm = null,
      riskOfReoffending = null,
    AssessmentNeedDto(
      section = NeedsSection.DRUG_MISUSE.name,
      name = NeedsSection.DRUG_MISUSE.description,
      score = 0,
      oasysThreshold = OasysThreshold(2),
    ),
    AssessmentNeedDetailDto(
      section = AssessmentSection.ATTITUDE.name,
      name = "Attitudes",
      needStatus = NeedStatus.NOT_IDENTIFIED_NEED,
      riskOfHarm = false,
      riskOfReoffending = false,
      score = 0,
      oasysThreshold = OasysThreshold(2),
    ),
    AssessmentNeedDetailDto(
      section = AssessmentSection.FINANCE.name,
      name = "Finance",
      needStatus = NeedStatus.UNSCORED_NEED,
      riskOfHarm = false,
      riskOfReoffending = false,
      score = null,
      oasysThreshold = null,
    ),
    AssessmentNeedDetailDto(
      section = AssessmentSection.EMOTIONAL_WELLBEING.name,
      name = "Emotional Well-being",
      needStatus = NeedStatus.UNSCORED_NEED,
      riskOfHarm = false,
      riskOfReoffending = false,
      score = null,
      oasysThreshold = null,
    ),
  )

  private fun sanNeedDetails() = listOf(
    AssessmentNeedDetailDto(
      section = AssessmentSection.PERSONAL_RELATIONSHIPS_AND_COMMUNITY.name,
      name = "Personal relationships and community",
      needStatus = NeedStatus.IDENTIFIED_NEED,
      riskOfHarm = false,
      riskOfReoffending = false,
      score = 3,
      oasysThreshold = OasysThreshold(2),
    ),
    AssessmentNeedDetailDto(
      section = AssessmentSection.THINKING_ATTITUDES_AND_BEHAVIOUR.name,
      name = "Thinking, behaviours and attitudes",
      needStatus = NeedStatus.IDENTIFIED_NEED,
      riskOfHarm = false,
      riskOfReoffending = false,
      score = 6,
      oasysThreshold = OasysThreshold(2),
    ),
    AssessmentNeedDetailDto(
      section = AssessmentSection.ACCOMMODATION.name,
      name = "Accommodation",
      needStatus = NeedStatus.NOT_IDENTIFIED_NEED,
      riskOfHarm = false,
      riskOfReoffending = false,
      score = 1,
      oasysThreshold = OasysThreshold(2),
    ),
    AssessmentNeedDetailDto(
      section = AssessmentSection.EMPLOYMENT_AND_EDUCATION.name,
      name = "Employment and education",
      needStatus = NeedStatus.NOT_IDENTIFIED_NEED,
      riskOfHarm = false,
      riskOfReoffending = false,
      score = 0,
      oasysThreshold = OasysThreshold(2),
    ),
    AssessmentNeedDetailDto(
      section = AssessmentSection.LIFESTYLE_AND_ASSOCIATES.name,
      name = "Lifestyle and associates",
      needStatus = NeedStatus.NOT_IDENTIFIED_NEED,
      riskOfHarm = null,
      riskOfReoffending = null,
      score = 0,
      oasysThreshold = OasysThreshold(2),
    ),
    AssessmentNeedDetailDto(
      section = AssessmentSection.DRUG_USE.name,
      name = "Drug use",
      needStatus = NeedStatus.NOT_IDENTIFIED_NEED,
      riskOfHarm = false,
      riskOfReoffending = false,
      score = 0,
      oasysThreshold = OasysThreshold(2),
    ),
    AssessmentNeedDetailDto(
      section = AssessmentSection.ALCOHOL_USE.name,
      name = "Alcohol use",
      needStatus = NeedStatus.NOT_IDENTIFIED_NEED,
      riskOfHarm = false,
      riskOfReoffending = false,
      score = 0,
      oasysThreshold = OasysThreshold(2),
    ),
    AssessmentNeedDetailDto(
      section = AssessmentSection.FINANCE.name,
      name = "Finance",
      needStatus = NeedStatus.UNSCORED_NEED,
      riskOfHarm = false,
      riskOfReoffending = false,
      score = null,
      oasysThreshold = null,
    ),
    AssessmentNeedDetailDto(
      section = AssessmentSection.HEALTH_AND_WELLBEING.name,
      name = "Health and wellbeing",
      needStatus = NeedStatus.UNSCORED_NEED,
      riskOfHarm = null,
      riskOfReoffending = null,
      score = null,
      oasysThreshold = null,
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
              assessmentType = AssessmentType.LAYER3,
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
              assessmentType = AssessmentType.LAYER3,
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
