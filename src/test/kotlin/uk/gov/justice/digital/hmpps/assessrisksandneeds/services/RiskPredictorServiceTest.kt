package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.MDC
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentStatus
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.CaseAccess
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.IdentifierType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskScoresDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrScoreSource.OASYS
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreLevel.HIGH
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreLevel.LOW
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreLevel.MEDIUM
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ogrs4.AllPredictorDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.config.RequestData
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.CommunityApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.OasysApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.AllRisksOasysRiskPredictorsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.AllRisksPredictorAssessmentDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysOgp2Dto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysOgpDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysOgrDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysOgrs4gDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysOgrs4vDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysOspDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysOvp2Dto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysOvpDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysRsrDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysSnsvDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.RisksCrAssOasysRiskPredictorsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.RisksCrAssPredictorAssessmentDto
import java.math.BigDecimal
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
@DisplayName("Risk Predictors Service Tests")
class RiskPredictorServiceTest {

  private val oasysApiClient: OasysApiRestClient = mockk()
  private val communityApiRestClient: CommunityApiRestClient = mockk()
  private val auditService: AuditService = mockk()
  private val objectMapper: ObjectMapper = mockk()

  @BeforeEach
  fun setup() {
    MDC.put(RequestData.USER_NAME_HEADER, "User name")
    every { objectMapper.writeValueAsString(any()) } returns sourceAnswersJson
    every { communityApiRestClient.verifyUserAccess(any(), any()) } answers {
      CaseAccess(
        it.invocation.args[0] as String,
        userExcluded = false,
        userRestricted = false,
        null,
        null,
      )
    }
    every { auditService.sendEvent(any(), any()) } returns Unit
  }

  @Nested
  @DisplayName("OASys Scores")
  inner class ValidateOASysScores {

    private val riskPredictorsService =
      RiskPredictorService(
        oasysApiClient,
        communityApiRestClient,
        auditService,
      )

    @Test
    fun `should return a list of all risk predictors for valid crn`() {
      // Given
      val crn = "X12345"

      val allRisksOasysRiskPredictorsDto = AllRisksOasysRiskPredictorsDto(
        listOf(
          provideVersionOneAllRisksOutput(),
        ),
      )

      every {
        oasysApiClient.getRiskPredictorsForCompletedAssessments(crn)
      }.returns(allRisksOasysRiskPredictorsDto)

      // When
      val allRiskScores = riskPredictorsService.getAllRiskScores(crn)

      // Should
      with(allRiskScores[0]) {
        assertThat(completedDate).isEqualTo(LocalDateTime.of(2025, 1, 1, 12, 0, 0))

        assertThat(violencePredictorScore?.ovpStaticWeightedScore).isEqualTo(BigDecimal(14))
        assertThat(violencePredictorScore?.ovpDynamicWeightedScore).isEqualTo(BigDecimal(3))
        assertThat(violencePredictorScore?.ovpTotalWeightedScore).isEqualTo(BigDecimal(17))
        assertThat(violencePredictorScore?.oneYear).isEqualTo(BigDecimal(4))
        assertThat(violencePredictorScore?.twoYears).isEqualTo(BigDecimal(7))
        assertThat(violencePredictorScore?.ovpRisk).isEqualTo(LOW)

        assertThat(groupReconvictionScore?.oneYear).isEqualTo(BigDecimal(3))
        assertThat(groupReconvictionScore?.twoYears).isEqualTo(BigDecimal(5))
        assertThat(groupReconvictionScore?.scoreLevel).isEqualTo(LOW)

        assertThat(riskOfSeriousRecidivismScore?.percentageScore).isEqualTo(BigDecimal.valueOf(50.1234))
        assertThat(riskOfSeriousRecidivismScore?.staticOrDynamic).isEqualTo(ScoreType.DYNAMIC)
        assertThat(riskOfSeriousRecidivismScore?.source).isEqualTo(OASYS)
        assertThat(riskOfSeriousRecidivismScore?.algorithmVersion).isEqualTo("5")
        assertThat(riskOfSeriousRecidivismScore?.scoreLevel).isEqualTo(MEDIUM)

        assertThat(generalPredictorScore?.ogpStaticWeightedScore).isEqualTo(BigDecimal(3))
        assertThat(generalPredictorScore?.ogpDynamicWeightedScore).isEqualTo(BigDecimal(7))
        assertThat(generalPredictorScore?.ogpTotalWeightedScore).isEqualTo(BigDecimal(10))
        assertThat(generalPredictorScore?.ogp1Year).isEqualTo(BigDecimal(4))
        assertThat(generalPredictorScore?.ogp2Year).isEqualTo(BigDecimal(8))
        assertThat(generalPredictorScore?.ogpRisk).isEqualTo(LOW)

        assertThat(sexualPredictorScore?.ospIndecentPercentageScore).isEqualTo(BigDecimal.valueOf(2.81))
        assertThat(sexualPredictorScore?.ospContactPercentageScore).isEqualTo(BigDecimal.valueOf(1.07))
        assertThat(sexualPredictorScore?.ospIndecentScoreLevel).isEqualTo(MEDIUM)
        assertThat(sexualPredictorScore?.ospContactScoreLevel).isEqualTo(MEDIUM)
      }
    }

    @Test
    fun `should return an empty list of all risk predictors for invalid crn`() {
      // Given
      val crn = "X12345"
      every {
        oasysApiClient.getRiskPredictorsForCompletedAssessments(crn)
      }.returns(null)

      // When
      val allRiskScores = riskPredictorsService.getAllRiskScores(crn)

      // Should
      assertThat(allRiskScores.isEmpty())
    }

    @Test
    fun `should return a list of all risk predictors versioned for valid crn`() {
      // Given
      val crn = "X12345"

      val allRisksOasysRiskPredictorsDto = AllRisksOasysRiskPredictorsDto(
        listOf(
          provideVersionOneAllRisksOutput(),
        ),
      )

      every {
        oasysApiClient.getRiskPredictorsForCompletedAssessments(crn)
      }.returns(allRisksOasysRiskPredictorsDto)

      // When
      val allRiskScores = riskPredictorsService.getAllRiskScores(IdentifierType.CRN, crn)

      val result = allRiskScores[0]
      assertThat(result.completedDate).isEqualTo(LocalDateTime.of(2025, 1, 1, 12, 0, 0))
      assertThat(result.outputVersion).isEqualTo("1")
      val outputTyped = result.output as RiskScoresDto

      // Should
      with(outputTyped) {
        assertThat(violencePredictorScore?.ovpStaticWeightedScore).isEqualTo(BigDecimal(14))
        assertThat(violencePredictorScore?.ovpDynamicWeightedScore).isEqualTo(BigDecimal(3))
        assertThat(violencePredictorScore?.ovpTotalWeightedScore).isEqualTo(BigDecimal(17))
        assertThat(violencePredictorScore?.oneYear).isEqualTo(BigDecimal(4))
        assertThat(violencePredictorScore?.twoYears).isEqualTo(BigDecimal(7))
        assertThat(violencePredictorScore?.ovpRisk).isEqualTo(LOW)

        assertThat(groupReconvictionScore?.oneYear).isEqualTo(BigDecimal(3))
        assertThat(groupReconvictionScore?.twoYears).isEqualTo(BigDecimal(5))
        assertThat(groupReconvictionScore?.scoreLevel).isEqualTo(LOW)

        assertThat(riskOfSeriousRecidivismScore?.percentageScore).isEqualTo(BigDecimal.valueOf(50.1234))
        assertThat(riskOfSeriousRecidivismScore?.staticOrDynamic).isEqualTo(ScoreType.DYNAMIC)
        assertThat(riskOfSeriousRecidivismScore?.source).isEqualTo(OASYS)
        assertThat(riskOfSeriousRecidivismScore?.algorithmVersion).isEqualTo("5")
        assertThat(riskOfSeriousRecidivismScore?.scoreLevel).isEqualTo(MEDIUM)

        assertThat(generalPredictorScore?.ogpStaticWeightedScore).isEqualTo(BigDecimal(3))
        assertThat(generalPredictorScore?.ogpDynamicWeightedScore).isEqualTo(BigDecimal(7))
        assertThat(generalPredictorScore?.ogpTotalWeightedScore).isEqualTo(BigDecimal(10))
        assertThat(generalPredictorScore?.ogp1Year).isEqualTo(BigDecimal(4))
        assertThat(generalPredictorScore?.ogp2Year).isEqualTo(BigDecimal(8))
        assertThat(generalPredictorScore?.ogpRisk).isEqualTo(LOW)

        assertThat(sexualPredictorScore?.ospIndecentPercentageScore).isEqualTo(BigDecimal.valueOf(2.81))
        assertThat(sexualPredictorScore?.ospContactPercentageScore).isEqualTo(BigDecimal.valueOf(1.07))
        assertThat(sexualPredictorScore?.ospIndecentScoreLevel).isEqualTo(MEDIUM)
        assertThat(sexualPredictorScore?.ospContactScoreLevel).isEqualTo(MEDIUM)
      }
    }

    @Test
    fun `should return an empty list of all risk predictors versioned for invalid crn`() {
      // Given
      val crn = "X12345"
      every {
        oasysApiClient.getRiskPredictorsForCompletedAssessments(crn)
      }.returns(null)

      // When
      val allRiskScores = riskPredictorsService.getAllRiskScores(IdentifierType.CRN, crn)

      // Should
      assertThat(allRiskScores.isEmpty())
    }

    @Test
    fun `should return legacy risk predictors for legacy assessment ID`() {
      // Given
      val id = 1234567890L

      val risksCrAssOasysRiskPredictorsDto = RisksCrAssOasysRiskPredictorsDto(
        "X123456",
        listOf(
          provideVersionOneRisksCrAssOutput(),
        ),
      )

      every {
        oasysApiClient.getRiskPredictorsByAssessmentId(id)
      }.returns(risksCrAssOasysRiskPredictorsDto)

      // When
      val result = riskPredictorsService.getAllRiskScoresByAssessmentId(id)

      assertThat(result.outputVersion).isEqualTo("1")
      val outputTyped = result.output as RiskScoresDto

      // Should
      with(outputTyped) {
        assertThat(violencePredictorScore?.ovpStaticWeightedScore).isEqualTo(BigDecimal(14))
        assertThat(violencePredictorScore?.ovpDynamicWeightedScore).isEqualTo(BigDecimal(3))
        assertThat(violencePredictorScore?.ovpTotalWeightedScore).isEqualTo(BigDecimal(17))
        assertThat(violencePredictorScore?.oneYear).isEqualTo(BigDecimal(4))
        assertThat(violencePredictorScore?.twoYears).isEqualTo(BigDecimal(7))
        assertThat(violencePredictorScore?.ovpRisk).isEqualTo(LOW)

        assertThat(groupReconvictionScore?.oneYear).isEqualTo(BigDecimal(3))
        assertThat(groupReconvictionScore?.twoYears).isEqualTo(BigDecimal(5))
        assertThat(groupReconvictionScore?.scoreLevel).isEqualTo(LOW)

        assertThat(riskOfSeriousRecidivismScore?.percentageScore).isEqualTo(BigDecimal.valueOf(50.1234))
        assertThat(riskOfSeriousRecidivismScore?.staticOrDynamic).isEqualTo(ScoreType.DYNAMIC)
        assertThat(riskOfSeriousRecidivismScore?.source).isEqualTo(OASYS)
        assertThat(riskOfSeriousRecidivismScore?.algorithmVersion).isEqualTo("5")
        assertThat(riskOfSeriousRecidivismScore?.scoreLevel).isEqualTo(MEDIUM)

        assertThat(generalPredictorScore?.ogpStaticWeightedScore).isEqualTo(BigDecimal(3))
        assertThat(generalPredictorScore?.ogpDynamicWeightedScore).isEqualTo(BigDecimal(7))
        assertThat(generalPredictorScore?.ogpTotalWeightedScore).isEqualTo(BigDecimal(10))
        assertThat(generalPredictorScore?.ogp1Year).isEqualTo(BigDecimal(4))
        assertThat(generalPredictorScore?.ogp2Year).isEqualTo(BigDecimal(8))
        assertThat(generalPredictorScore?.ogpRisk).isEqualTo(LOW)

        assertThat(sexualPredictorScore?.ospIndecentPercentageScore).isEqualTo(BigDecimal.valueOf(2.81))
        assertThat(sexualPredictorScore?.ospContactPercentageScore).isEqualTo(BigDecimal.valueOf(1.07))
        assertThat(sexualPredictorScore?.ospIndecentScoreLevel).isEqualTo(MEDIUM)
        assertThat(sexualPredictorScore?.ospContactScoreLevel).isEqualTo(MEDIUM)
      }
    }

    @Test
    fun `should return OGRS4 STATIC risk predictors for ogrs4 STATIC assessment ID`() {
      // Given
      val id = 1234567890L

      val risksCrAssOasysRiskPredictorsDto = RisksCrAssOasysRiskPredictorsDto(
        "X123456",
        listOf(
          provideVersionTwoStaticRisksCrAssOutput(),
        ),
      )

      every {
        oasysApiClient.getRiskPredictorsByAssessmentId(id)
      }.returns(risksCrAssOasysRiskPredictorsDto)

      // When
      val result = riskPredictorsService.getAllRiskScoresByAssessmentId(id)

      assertThat(result.outputVersion).isEqualTo("2")
      val outputTyped = result.output as AllPredictorDto

      // Should
      with(outputTyped) {
        assertThat(allReoffendingPredictor?.score).isEqualTo(BigDecimal.valueOf(1.23))
        assertThat(allReoffendingPredictor?.band).isEqualTo(MEDIUM)
        assertThat(allReoffendingPredictor?.staticOrDynamic).isEqualTo(ScoreType.STATIC)

        assertThat(violentReoffendingPredictor?.score).isEqualTo(BigDecimal.valueOf(1.34))
        assertThat(violentReoffendingPredictor?.band).isEqualTo(MEDIUM)
        assertThat(violentReoffendingPredictor?.staticOrDynamic).isEqualTo(ScoreType.STATIC)

        assertThat(seriousViolentReoffendingPredictor?.score).isEqualTo(BigDecimal.valueOf(1.45))
        assertThat(seriousViolentReoffendingPredictor?.band).isEqualTo(MEDIUM)
        assertThat(seriousViolentReoffendingPredictor?.staticOrDynamic).isEqualTo(ScoreType.STATIC)

        assertThat(directContactSexualReoffendingPredictor?.score).isEqualTo(BigDecimal.valueOf(2.81))
        assertThat(directContactSexualReoffendingPredictor?.band).isEqualTo(MEDIUM)

        assertThat(indirectImageContactSexualReoffendingPredictor?.score).isEqualTo(BigDecimal.valueOf(1.07))
        assertThat(indirectImageContactSexualReoffendingPredictor?.band).isEqualTo(MEDIUM)

        assertThat(combinedSeriousReoffendingPredictor?.score).isEqualTo(BigDecimal.valueOf(2.34))
        assertThat(combinedSeriousReoffendingPredictor?.band).isEqualTo(LOW)
        assertThat(combinedSeriousReoffendingPredictor?.staticOrDynamic).isEqualTo(ScoreType.STATIC)
        assertThat(combinedSeriousReoffendingPredictor?.algorithmVersion).isEqualTo("6")
      }
    }

    @Test
    fun `should return OGRS4 DYNAMIC risk predictors for ogrs4 DYNAMIC assessment ID`() {
      // Given
      val id = 1234567890L

      val risksCrAssOasysRiskPredictorsDto = RisksCrAssOasysRiskPredictorsDto(
        "X123456",
        listOf(
          provideVersionTwoDynamicRisksCrAssOutput(),
        ),
      )

      every {
        oasysApiClient.getRiskPredictorsByAssessmentId(id)
      }.returns(risksCrAssOasysRiskPredictorsDto)

      // When
      val result = riskPredictorsService.getAllRiskScoresByAssessmentId(id)

      assertThat(result.outputVersion).isEqualTo("2")
      val outputTyped = result.output as AllPredictorDto

      // Should
      with(outputTyped) {
        assertThat(allReoffendingPredictor?.score).isEqualTo(BigDecimal.valueOf(10.23))
        assertThat(allReoffendingPredictor?.band).isEqualTo(HIGH)
        assertThat(allReoffendingPredictor?.staticOrDynamic).isEqualTo(ScoreType.DYNAMIC)

        assertThat(violentReoffendingPredictor?.score).isEqualTo(BigDecimal.valueOf(10.34))
        assertThat(violentReoffendingPredictor?.band).isEqualTo(HIGH)
        assertThat(violentReoffendingPredictor?.staticOrDynamic).isEqualTo(ScoreType.DYNAMIC)

        assertThat(seriousViolentReoffendingPredictor?.score).isEqualTo(BigDecimal.valueOf(40.23))
        assertThat(seriousViolentReoffendingPredictor?.band).isEqualTo(HIGH)
        assertThat(seriousViolentReoffendingPredictor?.staticOrDynamic).isEqualTo(ScoreType.DYNAMIC)

        assertThat(directContactSexualReoffendingPredictor?.score).isEqualTo(BigDecimal.valueOf(2.81))
        assertThat(directContactSexualReoffendingPredictor?.band).isEqualTo(MEDIUM)

        assertThat(indirectImageContactSexualReoffendingPredictor?.score).isEqualTo(BigDecimal.valueOf(1.07))
        assertThat(indirectImageContactSexualReoffendingPredictor?.band).isEqualTo(MEDIUM)

        assertThat(combinedSeriousReoffendingPredictor?.score).isEqualTo(BigDecimal.valueOf(50.1234))
        assertThat(combinedSeriousReoffendingPredictor?.band).isEqualTo(MEDIUM)
        assertThat(combinedSeriousReoffendingPredictor?.staticOrDynamic).isEqualTo(ScoreType.DYNAMIC)
        assertThat(combinedSeriousReoffendingPredictor?.algorithmVersion).isEqualTo("6")
      }
    }
  }

  fun provideVersionOneAllRisksOutput(): AllRisksPredictorAssessmentDto = AllRisksPredictorAssessmentDto(
    dateCompleted = LocalDateTime.of(2025, 1, 1, 12, 0, 0),
    assessmentType = "LAYER3",
    assessmentStatus = AssessmentStatus.COMPLETE,
    ogpScoreDto = OasysOgpDto(
      ogpStWesc = BigDecimal.valueOf(3),
      ogpDyWesc = BigDecimal.valueOf(7),
      ogpTotWesc = BigDecimal.valueOf(10),
      ogp1Year = BigDecimal.valueOf(4),
      ogp2Year = BigDecimal.valueOf(8),
      ogpRisk = LOW.type,
    ),
    ovpScoreDto = OasysOvpDto(
      ovpStWesc = BigDecimal.valueOf(14),
      ovpDyWesc = BigDecimal.valueOf(3),
      ovpTotWesc = BigDecimal.valueOf(17),
      ovp1Year = BigDecimal.valueOf(4),
      ovp2Year = BigDecimal.valueOf(7),
      ovpRisk = LOW.type,
    ),
    ogrScoreDto = OasysOgrDto(
      ogrs31Year = BigDecimal.valueOf(3),
      ogrs32Year = BigDecimal.valueOf(5),
      ogrs3RiskRecon = LOW.type,
    ),
    rsrScoreDto = OasysRsrDto(
      rsrPercentageScore = BigDecimal.valueOf(50.1234),
      rsrStaticOrDynamic = ScoreType.DYNAMIC,
      rsrAlgorithmVersion = "5",
      scoreLevel = MEDIUM.type,
    ),
    ospScoreDto = OasysOspDto(
      ospImagePercentageScore = BigDecimal.valueOf(2.81),
      ospContactPercentageScore = BigDecimal.valueOf(1.07),
      ospImageScoreLevel = MEDIUM.type,
      ospContactScoreLevel = MEDIUM.type,
    ),
    ogrs4gScoreDto = null,
    ogrs4vScoreDto = null,
    ogp2ScoreDto = null,
    ovp2ScoreDto = null,
    snsvScoreDto = null,
  )

  fun provideVersionTwoStaticAllRisksOutput(): AllRisksPredictorAssessmentDto = AllRisksPredictorAssessmentDto(
    dateCompleted = LocalDateTime.of(2025, 1, 2, 12, 0, 0),
    assessmentType = "LAYER3",
    assessmentStatus = AssessmentStatus.COMPLETE,
    ogpScoreDto = OasysOgpDto(
      ogpStWesc = BigDecimal.valueOf(3),
      ogpDyWesc = BigDecimal.valueOf(7),
      ogpTotWesc = BigDecimal.valueOf(10),
      ogp1Year = BigDecimal.valueOf(4),
      ogp2Year = BigDecimal.valueOf(8),
      ogpRisk = LOW.type,
    ),
    ovpScoreDto = OasysOvpDto(
      ovpStWesc = BigDecimal.valueOf(14),
      ovpDyWesc = BigDecimal.valueOf(3),
      ovpTotWesc = BigDecimal.valueOf(17),
      ovp1Year = BigDecimal.valueOf(4),
      ovp2Year = BigDecimal.valueOf(7),
      ovpRisk = LOW.type,
    ),
    ogrScoreDto = OasysOgrDto(
      ogrs31Year = BigDecimal.valueOf(3),
      ogrs32Year = BigDecimal.valueOf(5),
      ogrs3RiskRecon = LOW.type,
    ),
    rsrScoreDto = OasysRsrDto(
      rsrPercentageScore = BigDecimal.valueOf(2.34),
      rsrStaticOrDynamic = ScoreType.STATIC,
      rsrAlgorithmVersion = "6",
      scoreLevel = LOW.type,
    ),
    ospScoreDto = OasysOspDto(
      ospDirectContactPercentageScore = BigDecimal.valueOf(2.81),
      ospIndirectImagesChildrenPercentageScore = BigDecimal.valueOf(1.07),
      ospDirectContactScoreLevel = MEDIUM.type,
      ospIndirectImagesChildrenScoreLevel = MEDIUM.type,
    ),
    ogrs4gScoreDto = OasysOgrs4gDto(
      ogrs4gYr2 = BigDecimal.valueOf(1.23),
      ogrs4gBand = MEDIUM.type,
      ogrs4gCalculated = "Y",
    ),
    ogrs4vScoreDto = OasysOgrs4vDto(
      ogrs4vYr2 = BigDecimal.valueOf(1.34),
      ogrs4vBand = MEDIUM.type,
      ogrs4vCalculated = "Y",
    ),
    ogp2ScoreDto = OasysOgp2Dto(
      ogp2Yr2 = BigDecimal.valueOf(10.23),
      ogp2Band = HIGH.type,
      ogp2Calculated = "N",
    ),
    ovp2ScoreDto = OasysOvp2Dto(
      ovp2Yr2 = BigDecimal.valueOf(10.34),
      ovp2Band = HIGH.type,
      ovp2Calculated = "N",
    ),
    snsvScoreDto = OasysSnsvDto(
      snsvStaticYr2 = BigDecimal.valueOf(1.45),
      snsvDynamicYr2 = BigDecimal.valueOf(40.23),
      snsvStaticYr2Band = MEDIUM.type,
      snsvDynamicYr2Band = HIGH.type,
      snsvStaticCalculated = "Y",
      snsvDynamicCalculated = "N",
    ),
  )

  fun provideVersionTwoDynamicAllRisksOutput(): AllRisksPredictorAssessmentDto = AllRisksPredictorAssessmentDto(
    dateCompleted = LocalDateTime.of(2025, 1, 3, 12, 0, 0),
    assessmentType = "LAYER3",
    assessmentStatus = AssessmentStatus.COMPLETE,
    ogpScoreDto = OasysOgpDto(
      ogpStWesc = BigDecimal.valueOf(3),
      ogpDyWesc = BigDecimal.valueOf(7),
      ogpTotWesc = BigDecimal.valueOf(10),
      ogp1Year = BigDecimal.valueOf(4),
      ogp2Year = BigDecimal.valueOf(8),
      ogpRisk = LOW.type,
    ),
    ovpScoreDto = OasysOvpDto(
      ovpStWesc = BigDecimal.valueOf(14),
      ovpDyWesc = BigDecimal.valueOf(3),
      ovpTotWesc = BigDecimal.valueOf(17),
      ovp1Year = BigDecimal.valueOf(4),
      ovp2Year = BigDecimal.valueOf(7),
      ovpRisk = LOW.type,
    ),
    ogrScoreDto = OasysOgrDto(
      ogrs31Year = BigDecimal.valueOf(3),
      ogrs32Year = BigDecimal.valueOf(5),
      ogrs3RiskRecon = LOW.type,
    ),
    rsrScoreDto = OasysRsrDto(
      rsrPercentageScore = BigDecimal.valueOf(50.1234),
      rsrStaticOrDynamic = ScoreType.DYNAMIC,
      rsrAlgorithmVersion = "6",
      scoreLevel = MEDIUM.type,
    ),
    ospScoreDto = OasysOspDto(
      ospDirectContactPercentageScore = BigDecimal.valueOf(2.81),
      ospIndirectImagesChildrenPercentageScore = BigDecimal.valueOf(1.07),
      ospDirectContactScoreLevel = MEDIUM.type,
      ospIndirectImagesChildrenScoreLevel = MEDIUM.type,
    ),
    ogrs4gScoreDto = OasysOgrs4gDto(
      ogrs4gYr2 = BigDecimal.valueOf(1.23),
      ogrs4gBand = MEDIUM.type,
      ogrs4gCalculated = "Y",
    ),
    ogrs4vScoreDto = OasysOgrs4vDto(
      ogrs4vYr2 = BigDecimal.valueOf(1.34),
      ogrs4vBand = MEDIUM.type,
      ogrs4vCalculated = "Y",
    ),
    ogp2ScoreDto = OasysOgp2Dto(
      ogp2Yr2 = BigDecimal.valueOf(10.23),
      ogp2Band = HIGH.type,
      ogp2Calculated = "Y",
    ),
    ovp2ScoreDto = OasysOvp2Dto(
      ovp2Yr2 = BigDecimal.valueOf(10.34),
      ovp2Band = HIGH.type,
      ovp2Calculated = "Y",
    ),
    snsvScoreDto = OasysSnsvDto(
      snsvStaticYr2 = BigDecimal.valueOf(1.45),
      snsvDynamicYr2 = BigDecimal.valueOf(40.23),
      snsvStaticYr2Band = MEDIUM.type,
      snsvDynamicYr2Band = HIGH.type,
      snsvStaticCalculated = "Y",
      snsvDynamicCalculated = "Y",
    ),
  )

  fun provideVersionOneRisksCrAssOutput(): RisksCrAssPredictorAssessmentDto = RisksCrAssPredictorAssessmentDto(
    ogpScoreDto = OasysOgpDto(
      ogpStWesc = BigDecimal.valueOf(3),
      ogpDyWesc = BigDecimal.valueOf(7),
      ogpTotWesc = BigDecimal.valueOf(10),
      ogp1Year = BigDecimal.valueOf(4),
      ogp2Year = BigDecimal.valueOf(8),
      ogpRisk = LOW.type,
    ),
    ovpScoreDto = OasysOvpDto(
      ovpStWesc = BigDecimal.valueOf(14),
      ovpDyWesc = BigDecimal.valueOf(3),
      ovpTotWesc = BigDecimal.valueOf(17),
      ovp1Year = BigDecimal.valueOf(4),
      ovp2Year = BigDecimal.valueOf(7),
      ovpRisk = LOW.type,
    ),
    ogrScoreDto = OasysOgrDto(
      ogrs31Year = BigDecimal.valueOf(3),
      ogrs32Year = BigDecimal.valueOf(5),
      ogrs3RiskRecon = LOW.type,
    ),
    rsrScoreDto = OasysRsrDto(
      rsrPercentageScore = BigDecimal.valueOf(50.1234),
      rsrStaticOrDynamic = ScoreType.DYNAMIC,
      rsrAlgorithmVersion = "5",
      scoreLevel = MEDIUM.type,
    ),
    ospScoreDto = OasysOspDto(
      ospImagePercentageScore = BigDecimal.valueOf(2.81),
      ospContactPercentageScore = BigDecimal.valueOf(1.07),
      ospImageScoreLevel = MEDIUM.type,
      ospContactScoreLevel = MEDIUM.type,
    ),
    ogrs4gScoreDto = null,
    ogrs4vScoreDto = null,
    ogp2ScoreDto = null,
    ovp2ScoreDto = null,
    snsvScoreDto = null,
  )

  fun provideVersionTwoStaticRisksCrAssOutput(): RisksCrAssPredictorAssessmentDto = RisksCrAssPredictorAssessmentDto(
    ogpScoreDto = OasysOgpDto(
      ogpStWesc = BigDecimal.valueOf(3),
      ogpDyWesc = BigDecimal.valueOf(7),
      ogpTotWesc = BigDecimal.valueOf(10),
      ogp1Year = BigDecimal.valueOf(4),
      ogp2Year = BigDecimal.valueOf(8),
      ogpRisk = LOW.type,
    ),
    ovpScoreDto = OasysOvpDto(
      ovpStWesc = BigDecimal.valueOf(14),
      ovpDyWesc = BigDecimal.valueOf(3),
      ovpTotWesc = BigDecimal.valueOf(17),
      ovp1Year = BigDecimal.valueOf(4),
      ovp2Year = BigDecimal.valueOf(7),
      ovpRisk = LOW.type,
    ),
    ogrScoreDto = OasysOgrDto(
      ogrs31Year = BigDecimal.valueOf(3),
      ogrs32Year = BigDecimal.valueOf(5),
      ogrs3RiskRecon = LOW.type,
    ),
    rsrScoreDto = OasysRsrDto(
      rsrPercentageScore = BigDecimal.valueOf(2.34),
      rsrStaticOrDynamic = ScoreType.STATIC,
      rsrAlgorithmVersion = "6",
      scoreLevel = LOW.type,
    ),
    ospScoreDto = OasysOspDto(
      ospDirectContactPercentageScore = BigDecimal.valueOf(2.81),
      ospIndirectImagesChildrenPercentageScore = BigDecimal.valueOf(1.07),
      ospDirectContactScoreLevel = MEDIUM.type,
      ospIndirectImagesChildrenScoreLevel = MEDIUM.type,
    ),
    ogrs4gScoreDto = OasysOgrs4gDto(
      ogrs4gYr2 = BigDecimal.valueOf(1.23),
      ogrs4gBand = MEDIUM.type,
      ogrs4gCalculated = "Y",
    ),
    ogrs4vScoreDto = OasysOgrs4vDto(
      ogrs4vYr2 = BigDecimal.valueOf(1.34),
      ogrs4vBand = MEDIUM.type,
      ogrs4vCalculated = "Y",
    ),
    ogp2ScoreDto = OasysOgp2Dto(
      ogp2Yr2 = BigDecimal.valueOf(10.23),
      ogp2Band = HIGH.type,
      ogp2Calculated = "N",
    ),
    ovp2ScoreDto = OasysOvp2Dto(
      ovp2Yr2 = BigDecimal.valueOf(10.34),
      ovp2Band = HIGH.type,
      ovp2Calculated = "N",
    ),
    snsvScoreDto = OasysSnsvDto(
      snsvStaticYr2 = BigDecimal.valueOf(1.45),
      snsvDynamicYr2 = BigDecimal.valueOf(40.23),
      snsvStaticYr2Band = MEDIUM.type,
      snsvDynamicYr2Band = HIGH.type,
      snsvStaticCalculated = "Y",
      snsvDynamicCalculated = "N",
    ),
  )

  fun provideVersionTwoDynamicRisksCrAssOutput(): RisksCrAssPredictorAssessmentDto = RisksCrAssPredictorAssessmentDto(
    ogpScoreDto = OasysOgpDto(
      ogpStWesc = BigDecimal.valueOf(3),
      ogpDyWesc = BigDecimal.valueOf(7),
      ogpTotWesc = BigDecimal.valueOf(10),
      ogp1Year = BigDecimal.valueOf(4),
      ogp2Year = BigDecimal.valueOf(8),
      ogpRisk = LOW.type,
    ),
    ovpScoreDto = OasysOvpDto(
      ovpStWesc = BigDecimal.valueOf(14),
      ovpDyWesc = BigDecimal.valueOf(3),
      ovpTotWesc = BigDecimal.valueOf(17),
      ovp1Year = BigDecimal.valueOf(4),
      ovp2Year = BigDecimal.valueOf(7),
      ovpRisk = LOW.type,
    ),
    ogrScoreDto = OasysOgrDto(
      ogrs31Year = BigDecimal.valueOf(3),
      ogrs32Year = BigDecimal.valueOf(5),
      ogrs3RiskRecon = LOW.type,
    ),
    rsrScoreDto = OasysRsrDto(
      rsrPercentageScore = BigDecimal.valueOf(50.1234),
      rsrStaticOrDynamic = ScoreType.DYNAMIC,
      rsrAlgorithmVersion = "6",
      scoreLevel = MEDIUM.type,
    ),
    ospScoreDto = OasysOspDto(
      ospDirectContactPercentageScore = BigDecimal.valueOf(2.81),
      ospIndirectImagesChildrenPercentageScore = BigDecimal.valueOf(1.07),
      ospDirectContactScoreLevel = MEDIUM.type,
      ospIndirectImagesChildrenScoreLevel = MEDIUM.type,
    ),
    ogrs4gScoreDto = OasysOgrs4gDto(
      ogrs4gYr2 = BigDecimal.valueOf(1.23),
      ogrs4gBand = MEDIUM.type,
      ogrs4gCalculated = "Y",
    ),
    ogrs4vScoreDto = OasysOgrs4vDto(
      ogrs4vYr2 = BigDecimal.valueOf(1.34),
      ogrs4vBand = MEDIUM.type,
      ogrs4vCalculated = "Y",
    ),
    ogp2ScoreDto = OasysOgp2Dto(
      ogp2Yr2 = BigDecimal.valueOf(10.23),
      ogp2Band = HIGH.type,
      ogp2Calculated = "Y",
    ),
    ovp2ScoreDto = OasysOvp2Dto(
      ovp2Yr2 = BigDecimal.valueOf(10.34),
      ovp2Band = HIGH.type,
      ovp2Calculated = "Y",
    ),
    snsvScoreDto = OasysSnsvDto(
      snsvStaticYr2 = BigDecimal.valueOf(1.45),
      snsvDynamicYr2 = BigDecimal.valueOf(40.23),
      snsvStaticYr2Band = MEDIUM.type,
      snsvDynamicYr2Band = HIGH.type,
      snsvStaticCalculated = "Y",
      snsvDynamicCalculated = "Y",
    ),
  )

  companion object {
    val sourceAnswersJson =
      """{"gender":"MALE","dob":"2001-01-01","assessment_date":"2021-01-01T00:00:00","offence_code":"138","offence_subcode":"00","date_first_sanction":"2020-01-01","total_sanctions":10,"total_violent_offences":8,"date_current_conviction":"2020-12-18","any_sexual_offences":true,"current_sexual_offence":true,"current_offence_victim_stranger":true,"most_recent_sexual_offence_date":"2020-12-11","total_sexual_offences_adult":5,"total_sexual_offences_child":5,"total_sexual_offences_child_image":2,"total_non_contact_sexual_offences":2,"earliest_release_date":"2021-11-01","completed_interview":true,"suitable_accommodation":"MISSING","unemployed_on_release":"NOT_AVAILABLE_FOR_WORK","current_relationship_with_partner":"SIGNIFICANT_PROBLEMS","evidence_domestic_violence":true,"perpetrator_domestic_violence":true,"use_of_alcohol":"SIGNIFICANT_PROBLEMS","binge_drinking":"SIGNIFICANT_PROBLEMS","impulsivity_issues":"SOME_PROBLEMS","temper_control_issues":"SIGNIFICANT_PROBLEMS","pro_criminal_attitudes":"SOME_PROBLEMS","previous_murder_attempt":true,"previous_wounding":true,"previous_aggravated_burglary":true,"previous_arson":true,"previous_criminal_damage":true,"previous_kidnapping":true,"previous_possession_firearm":true,"previous_robbery":true,"previous_offence_weapon":true,"current_possession_firearm":true,"current_offence_weapon":true}  """.trimIndent()
  }
}
