package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.MDC
import org.springframework.http.HttpMethod
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.CurrentOffenceDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.CurrentOffencesDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.DynamicScoringOffencesDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.EmploymentType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.Gender
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OffenderAndOffencesDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PredictorSource
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PredictorSubType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PredictorType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PreviousOffencesDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ProblemsLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskPredictorsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrScoreSource.OASYS
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.Score
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreLevel.HIGH
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreLevel.LOW
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreLevel.MEDIUM
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreLevel.VERY_HIGH
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreType.STATIC
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.UserAccessResponse
import uk.gov.justice.digital.hmpps.assessrisksandneeds.config.RequestData
import uk.gov.justice.digital.hmpps.assessrisksandneeds.jpa.entities.OffenderPredictorsHistoryEntity
import uk.gov.justice.digital.hmpps.assessrisksandneeds.jpa.respositories.OffenderPredictorsHistoryRepository
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.AssessmentApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.CommunityApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.ExternalService
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysPredictorsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysRSRPredictorsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OgpDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.Ogrs3Dto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OspDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OvpDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.RefElementDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.RsrDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.ExternalApiEntityNotFoundException
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.IncorrectInputParametersException
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.PredictorCalculationError
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@ExtendWith(MockKExtension::class)
@DisplayName("Risk Predictors Service Tests")
class RiskPredictorServiceTest {

  private val assessmentApiClient: AssessmentApiRestClient = mockk()
  private val communityApiRestClient: CommunityApiRestClient = mockk()
  private val offenderPredictorsHistoryRepository: OffenderPredictorsHistoryRepository = mockk()
  private val objectMapper: ObjectMapper = mockk()

  private val offencesAndOffencesDto = OffenderAndOffencesDto(
    crn = "X1345",
    gender = Gender.MALE,
    dob = LocalDate.of(2000, 1, 1).minusYears(20),
    assessmentDate = LocalDateTime.of(2021, 1, 1, 0, 0, 0),
    currentOffence = CurrentOffenceDto("138", "00"),
    dateOfFirstSanction = LocalDate.of(2021, 1, 1).minusYears(1),
    totalOffences = 10,
    totalViolentOffences = 8,
    dateOfCurrentConviction = LocalDate.of(2021, 1, 1).minusWeeks(2),
    hasAnySexualOffences = true,
    isCurrentSexualOffence = true,
    isCurrentOffenceVictimStranger = true,
    mostRecentSexualOffenceDate = LocalDate.of(2021, 1, 1).minusWeeks(3),
    totalSexualOffencesInvolvingAnAdult = 5,
    totalSexualOffencesInvolvingAChild = 3,
    totalSexualOffencesInvolvingChildImages = 2,
    totalNonContactSexualOffences = 2,
    earliestReleaseDate = LocalDate.of(2021, 1, 1).plusMonths(10),
    hasCompletedInterview = true,
    dynamicScoringOffences = DynamicScoringOffencesDto(
      hasSuitableAccommodation = ProblemsLevel.MISSING,
      employment = EmploymentType.NOT_AVAILABLE_FOR_WORK,
      currentRelationshipWithPartner = ProblemsLevel.SIGNIFICANT_PROBLEMS,
      evidenceOfDomesticViolence = true,
      isPerpetrator = true,
      alcoholUseIssues = ProblemsLevel.SIGNIFICANT_PROBLEMS,
      bingeDrinkingIssues = ProblemsLevel.SIGNIFICANT_PROBLEMS,
      impulsivityIssues = ProblemsLevel.SOME_PROBLEMS,
      temperControlIssues = ProblemsLevel.SIGNIFICANT_PROBLEMS,
      proCriminalAttitudes = ProblemsLevel.SOME_PROBLEMS,
      previousOffences = PreviousOffencesDto(
        murderAttempt = true,
        wounding = true,
        aggravatedBurglary = true,
        arson = true,
        criminalDamage = true,
        kidnapping = true,
        firearmPossession = true,
        robbery = true,
        offencesWithWeapon = true
      ),
      currentOffences = CurrentOffencesDto(
        firearmPossession = true,
        offencesWithWeapon = true
      )
    )
  )

  @BeforeEach
  fun setup() {
    MDC.put(RequestData.USER_NAME_HEADER, "User name")
    every { objectMapper.writeValueAsString(any()) } returns sourceAnswersJson
    every { communityApiRestClient.verifyUserAccess(any(), any()) } returns UserAccessResponse(null, null, false, false)
  }

  @Nested
  @DisplayName("OASys Scores")
  inner class ValidateOASysScores {

    private val riskCalculatorService = OASysCalculatorServiceImpl(assessmentApiClient)
    private val riskPredictorsService =
      RiskPredictorService(
        assessmentApiClient,
        communityApiRestClient,
        offenderPredictorsHistoryRepository,
        riskCalculatorService,
        objectMapper
      )

    @Test
    fun `calculate risk predictors data throws PredictorCalculationError if calculation is null`() {
      val predictorType = PredictorType.RSR
      every {
        assessmentApiClient.calculatePredictorTypeScoring(predictorType, offencesAndOffencesDto)
      } returns null

      assertThrows<PredictorCalculationError> {
        riskPredictorsService.calculatePredictorScores(
          predictorType,
          offencesAndOffencesDto,
          false,
          PredictorSource.ASSESSMENTS_API,
          "sourceId"
        )
      }
    }

    @Test
    fun `should return a list of all risk predictors for valid crn`() {
      // Given
      val crn = "X12345"
      val now = LocalDateTime.now()

      val oasysPredictorsDto = OasysPredictorsDto(
        completedDate = now,
        ovp = OvpDto(
          ovpStaticWeightedScore = BigDecimal(1),
          ovpDynamicWeightedScore = BigDecimal(2),
          ovpTotalWeightedScore = BigDecimal(3),
          ovp1Year = BigDecimal(4),
          ovp2Year = BigDecimal(5),
          ovpRisk = RefElementDto(
            code = "ovpCode",
            shortDescription = "ovpShortDescription",
            description = "Low"
          )
        ),
        ogr3 = Ogrs3Dto(
          ogrs3_1Year = BigDecimal(6),
          ogrs3_2Year = BigDecimal(7),
          reconvictionRisk = RefElementDto(
            code = "ogrCode",
            shortDescription = "ogrShortDescription",
            description = "Medium"
          )
        ),
        rsr = RsrDto(
          rsrPercentageScore = BigDecimal(8),
          rsrStaticOrDynamic = "STATIC",
          rsrAlgorithmVersion = 98765,
          rsrRiskRecon = RefElementDto(
            code = "rsrCode",
            shortDescription = "rsrShortDescription",
            description = "High"
          )
        ),
        ogp = OgpDto(
          ogpStaticWeightedScore = BigDecimal(9),
          ogpDynamicWeightedScore = BigDecimal(10),
          ogpTotalWeightedScore = BigDecimal(11),
          ogp1Year = BigDecimal(12),
          ogp2Year = BigDecimal(13),
          ogpRisk = RefElementDto(
            code = "ogpCode",
            shortDescription = "ogpShortDescription",
            description = "Very High"
          )
        ),
        osp = OspDto(
          ospIndecentPercentageScore = BigDecimal(14),
          ospContactPercentageScore = BigDecimal(15),
          ospIndecentRiskRecon = RefElementDto(
            code = "ospCode",
            shortDescription = "ospShortDescription",
            description = "Very High"
          ),
          ospContactRiskRecon = RefElementDto(
            code = "ospCode",
            shortDescription = "ospShortDescription",
            description = "Low"
          )
        )
      )

      every {
        assessmentApiClient.getRiskScoresForCompletedLastYearAssessments(crn)
      }.returns(listOf(oasysPredictorsDto))

      // When
      val allRiskScores = riskPredictorsService.getAllRiskScores(crn)

      // Should
      with(allRiskScores[0]) {
        assertThat(completedDate).isEqualTo(now)

        assertThat(violencePredictorScore?.ovpStaticWeightedScore).isEqualTo(BigDecimal(1))
        assertThat(violencePredictorScore?.ovpDynamicWeightedScore).isEqualTo(BigDecimal(2))
        assertThat(violencePredictorScore?.ovpTotalWeightedScore).isEqualTo(BigDecimal(3))
        assertThat(violencePredictorScore?.oneYear).isEqualTo(BigDecimal(4))
        assertThat(violencePredictorScore?.twoYears).isEqualTo(BigDecimal(5))
        assertThat(violencePredictorScore?.ovpRisk).isEqualTo(LOW)

        assertThat(groupReconvictionScore?.oneYear).isEqualTo(BigDecimal(6))
        assertThat(groupReconvictionScore?.twoYears).isEqualTo(BigDecimal(7))
        assertThat(groupReconvictionScore?.scoreLevel).isEqualTo(MEDIUM)

        assertThat(riskOfSeriousRecidivismScore?.percentageScore).isEqualTo(BigDecimal(8))
        assertThat(riskOfSeriousRecidivismScore?.staticOrDynamic).isEqualTo(STATIC)
        assertThat(riskOfSeriousRecidivismScore?.source).isEqualTo(OASYS)
        assertThat(riskOfSeriousRecidivismScore?.algorithmVersion).isEqualTo("98765")
        assertThat(riskOfSeriousRecidivismScore?.scoreLevel).isEqualTo(HIGH)

        assertThat(generalPredictorScore?.ogpStaticWeightedScore).isEqualTo(BigDecimal(9))
        assertThat(generalPredictorScore?.ogpDynamicWeightedScore).isEqualTo(BigDecimal(10))
        assertThat(generalPredictorScore?.ogpTotalWeightedScore).isEqualTo(BigDecimal(11))
        assertThat(generalPredictorScore?.ogp1Year).isEqualTo(BigDecimal(12))
        assertThat(generalPredictorScore?.ogp2Year).isEqualTo(BigDecimal(13))
        assertThat(generalPredictorScore?.ogpRisk).isEqualTo(VERY_HIGH)

        assertThat(sexualPredictorScore?.ospIndecentPercentageScore).isEqualTo(BigDecimal(14))
        assertThat(sexualPredictorScore?.ospContactPercentageScore).isEqualTo(BigDecimal(15))
        assertThat(sexualPredictorScore?.ospIndecentScoreLevel).isEqualTo(VERY_HIGH)
        assertThat(sexualPredictorScore?.ospContactScoreLevel).isEqualTo(LOW)
      }
    }

    @Test
    fun `should return an empty list of all risk predictors for invalid crn`() {
      // Given
      val crn = "X12345"
      every {
        assessmentApiClient.getRiskScoresForCompletedLastYearAssessments(crn)
      }.returns(null)

      // When
      val allRiskScores = riskPredictorsService.getAllRiskScores(crn)

      // Should
      assertThat(allRiskScores.isEmpty())
    }

    @Test
    fun `calculate risk predictors data returns oasys predictors`() {
      val predictorType = PredictorType.RSR
      every {
        assessmentApiClient.calculatePredictorTypeScoring(predictorType, offencesAndOffencesDto)
      } returns OasysRSRPredictorsDto(
        algorithmVersion = 3,
        rsrScore = BigDecimal("11.34"),
        rsrBand = "High",
        scoreType = "Static",
        validRsrScore = "Y",
        ospcScore = BigDecimal("0"),
        ospcBand = "Not Applicable",
        validOspcScore = "A",
        ospiScore = BigDecimal("0"),
        ospiBand = "Not Applicable",
        validOspiScore = "A",
        errorCount = 0,
        calculationDateAndTime = LocalDateTime.of(2021, 7, 30, 16, 24, 25)
      )

      val predictorScores = riskPredictorsService.calculatePredictorScores(
        predictorType,
        offencesAndOffencesDto,
        false,
        PredictorSource.ASSESSMENTS_API,
        "sourceId"
      )

      assertThat(predictorScores.calculatedAt).isEqualTo(LocalDateTime.of(2021, 7, 30, 16, 24, 25))
      assertThat(predictorScores.type).isEqualTo(PredictorType.RSR)
      assertThat(predictorScores.scoreType).isEqualTo(STATIC)
      assertThat(predictorScores.scores[PredictorSubType.RSR]).isEqualTo(
        Score(
          level = HIGH, score = BigDecimal("11.34"), isValid = true
        )
      )
      assertThat(predictorScores.scores[PredictorSubType.OSPC]).isEqualTo(
        Score(
          level = ScoreLevel.NOT_APPLICABLE, score = BigDecimal("0"), isValid = false
        )
      )
      assertThat(predictorScores.scores[PredictorSubType.OSPI]).isEqualTo(
        Score(
          level = ScoreLevel.NOT_APPLICABLE, score = BigDecimal("0"), isValid = false
        )
      )
    }

    @Test
    fun `should deserialize errors correctly`() {
      val predictorType = PredictorType.RSR
      every {
        assessmentApiClient.calculatePredictorTypeScoring(predictorType, offencesAndOffencesDto)
      } returns OasysRSRPredictorsDto(
        algorithmVersion = 3,
        rsrScore = BigDecimal("11.34"),
        rsrBand = "High",
        scoreType = "Static",
        validRsrScore = "Y",
        ospcScore = BigDecimal("0"),
        ospcBand = "Not Applicable",
        validOspcScore = "A",
        ospiScore = BigDecimal("0"),
        ospiBand = "Not Applicable",
        validOspiScore = "A",
        errorCount = 9,
        errorMessage = "Missing detail on previous murder (R1.2).\nMissing detail on previous wounding or gbh (R1.2).\nMissing detail on previous kidnapping (R1.2).\nMissing detail on previous firearm (R1.2).\nMissing detail on previous robbery (R1.2).\nMissing detail on previous aggravated burglary (R1.2).\nMissing detail on previous weapon (R1.2).\nMissing detail on previous criminal damage with intent to endanger life (R1.2).\nMissing detail on previous arson (R1.2).\n",
        calculationDateAndTime = LocalDateTime.of(2021, 7, 30, 16, 24, 25)
      )

      val predictorScores = riskPredictorsService.calculatePredictorScores(
        predictorType,
        offencesAndOffencesDto,
        false,
        PredictorSource.ASSESSMENTS_API,
        "sourceId",
      )

      assertThat(predictorScores.errors).containsExactly(
        "Missing detail on previous murder (R1.2).",
        "Missing detail on previous wounding or gbh (R1.2).",
        "Missing detail on previous kidnapping (R1.2).",
        "Missing detail on previous firearm (R1.2).",
        "Missing detail on previous robbery (R1.2).",
        "Missing detail on previous aggravated burglary (R1.2).",
        "Missing detail on previous weapon (R1.2).",
        "Missing detail on previous criminal damage with intent to endanger life (R1.2).",
        "Missing detail on previous arson (R1.2).",
      )
    }

    @Test
    fun `calculate risk predictors data saves predictors when final version is true`() {
      val predictorType = PredictorType.RSR
      val algorithmVersion = "3"
      every {
        assessmentApiClient.calculatePredictorTypeScoring(predictorType, offencesAndOffencesDto, algorithmVersion)
      } returns OasysRSRPredictorsDto(
        algorithmVersion = 3,
        rsrScore = BigDecimal("11.34"),
        rsrBand = "High",
        scoreType = "Static",
        validRsrScore = "Y",
        ospcScore = BigDecimal("0"),
        ospcBand = "Not Applicable",
        validOspcScore = "A",
        ospiScore = BigDecimal("0"),
        ospiBand = "Not Applicable",
        validOspiScore = "A",
        errorCount = 0,
        calculationDateAndTime = LocalDateTime.of(2021, 7, 30, 16, 24, 25)
      )

      val source = PredictorSource.ASSESSMENTS_API
      val sourceId = "sourceId"
      val offenderPredictorsHistoryEntitySlot = slot<OffenderPredictorsHistoryEntity>()

      every {
        offenderPredictorsHistoryRepository.save(
          capture(offenderPredictorsHistoryEntitySlot)
        )
      } returns OffenderPredictorsHistoryEntity(
        offenderPredictorId = 1,
        offenderPredictorUuid = UUID.randomUUID(),
        predictorType = predictorType,
        algorithmVersion = algorithmVersion,
        calculatedAt = LocalDateTime.of(2021, 7, 30, 16, 24, 25),
        crn = "X1345",
        predictorTriggerSource = source,
        predictorTriggerSourceId = sourceId,
        createdBy = RequestData.getUserName(),
        assessmentCompletedDate = LocalDateTime.of(2021, 7, 30, 16, 0),
        scoreType = ScoreType.DYNAMIC
      )

      riskPredictorsService.calculatePredictorScores(
        predictorType,
        offencesAndOffencesDto,
        true,
        source,
        sourceId,
        algorithmVersion
      )

      with(offenderPredictorsHistoryEntitySlot.captured) {
        assertThat(algorithmVersion).isEqualTo(algorithmVersion)
        assertThat(predictorType).isEqualTo(predictorType)
        assertThat(calculatedAt).isEqualTo(LocalDateTime.of(2021, 7, 30, 16, 24, 25))
        assertThat(crn).isEqualTo("X1345")
        assertThat(predictorTriggerSource).isEqualTo(source)
        assertThat(predictorTriggerSourceId).isEqualTo(sourceId)
        assertThat(createdBy).isEqualTo(RequestData.getUserName())
        assertThat(predictors).hasSize(3)
        assertThat(predictors[0].predictorSubType).isEqualTo(PredictorSubType.RSR)
        assertThat(predictors[0].predictorScore).isEqualTo(BigDecimal("11.34"))
        assertThat(predictors[0].predictorLevel).isEqualTo(HIGH)
        assertThat(predictors[1].predictorSubType).isEqualTo(PredictorSubType.OSPC)
        assertThat(predictors[1].predictorScore).isEqualTo(BigDecimal("0"))
        assertThat(predictors[1].predictorLevel).isEqualTo(ScoreLevel.NOT_APPLICABLE)
        assertThat(predictors[2].predictorSubType).isEqualTo(PredictorSubType.OSPI)
        assertThat(predictors[2].predictorScore).isEqualTo(BigDecimal("0"))
        assertThat(predictors[2].predictorLevel).isEqualTo(ScoreLevel.NOT_APPLICABLE)
      }
    }

    @Test
    fun `calculate risk predictors final version throws IncorrectInputParametersException if crn is null`() {
      val predictorType = PredictorType.RSR

      assertThrows<IncorrectInputParametersException> {
        riskPredictorsService.calculatePredictorScores(
          predictorType,
          offencesAndOffencesDto.copy(crn = null),
          true,
          PredictorSource.ASSESSMENTS_API,
          "sourceId"
        )
      }
    }

    @Test
    fun `should NOT call risk predictor service when user is forbidden to access CRN`() {
      // Given
      val crn = "X12345"
      every { communityApiRestClient.verifyUserAccess(crn, any()) }.throws(ExternalApiEntityNotFoundException(msg = "User cannot access $crn", HttpMethod.GET, "url", ExternalService.COMMUNITY_API))

      // When
      assertThrows<ExternalApiEntityNotFoundException> { riskPredictorsService.getAllRiskScores(crn) }

      // Then
      verify(exactly = 0) { assessmentApiClient.getRiskScoresForCompletedLastYearAssessments(crn) }
    }
  }

  @Nested
  @DisplayName("ONNX RSR Calculator")
  inner class ValidateONNXRSRScores {

    private val riskCalculatorService: OnnxCalculatorServiceImpl = mockk()
    private val riskPredictorsService =
      RiskPredictorService(
        assessmentApiClient,
        communityApiRestClient,
        offenderPredictorsHistoryRepository,
        riskCalculatorService,
        objectMapper
      )

    @Test
    fun `calculate risk predictors data returns onnx predictors`() {
      val predictorType = PredictorType.RSR
      every {
        riskCalculatorService.calculatePredictorScores(predictorType, offencesAndOffencesDto)
      } returns RiskPredictorsDto(
        scoreType = ScoreType.DYNAMIC,
        algorithmVersion = "1",
        errorCount = 0,
        calculatedAt = LocalDateTime.of(2021, 7, 30, 16, 24, 25),
        type = PredictorType.RSR,
        errors = emptyList(),
        scores = mapOf(
          PredictorSubType.RSR to Score(LOW, BigDecimal("1.50"), true),
          PredictorSubType.OSPC to Score(MEDIUM, BigDecimal("2.50"), true),
          PredictorSubType.OSPI to Score(HIGH, BigDecimal("3.50"), true),
          PredictorSubType.SNSV to Score(null, BigDecimal("4.50"), true),

          PredictorSubType.RSR_2YR_BRIEF to Score(HIGH, BigDecimal("0.001"), true),
          PredictorSubType.RSR_1YR_BRIEF to Score(null, BigDecimal("0.002"), true),
          PredictorSubType.RSR_1YR_EXTENDED to Score(null, BigDecimal("0.003"), true),
          PredictorSubType.RSR_2YR_EXTENDED to Score(HIGH, BigDecimal("0.004"), true),

          PredictorSubType.OSPI_1YR to Score(null, BigDecimal("0.005"), true),
          PredictorSubType.OSPI_2YR to Score(HIGH, BigDecimal("0.006"), true),
          PredictorSubType.OSPC_1YR to Score(null, BigDecimal("0.007"), true),
          PredictorSubType.OSPC_2YR to Score(HIGH, BigDecimal("0.008"), true),

          PredictorSubType.SNSV_1YR_BRIEF to Score(null, BigDecimal("0.009"), true),
          PredictorSubType.SNSV_2YR_BRIEF to Score(null, BigDecimal("0.010"), true),
          PredictorSubType.SNSV_1YR_EXTENDED to Score(null, BigDecimal("0.011"), true),
          PredictorSubType.SNSV_2YR_EXTENDED to Score(null, BigDecimal("0.012"), true)
        )
      )

      val predictorScores = riskPredictorsService.calculatePredictorScores(
        predictorType,
        offencesAndOffencesDto,
        false,
        PredictorSource.ASSESSMENTS_API,
        "sourceId"
      )

      assertThat(predictorScores.calculatedAt).isEqualTo(LocalDateTime.of(2021, 7, 30, 16, 24, 25))
      assertThat(predictorScores.type).isEqualTo(PredictorType.RSR)
      assertThat(predictorScores.scoreType).isEqualTo(ScoreType.DYNAMIC)
      assertThat(predictorScores.scores[PredictorSubType.RSR]).isEqualTo(
        Score(level = LOW, score = BigDecimal("1.50"), isValid = true)
      )
      assertThat(predictorScores.scores[PredictorSubType.OSPC]).isEqualTo(
        Score(level = MEDIUM, score = BigDecimal("2.50"), isValid = true)
      )
      assertThat(predictorScores.scores[PredictorSubType.OSPI]).isEqualTo(
        Score(level = HIGH, score = BigDecimal("3.50"), isValid = true)
      )
      assertThat(predictorScores.scores[PredictorSubType.SNSV]).isEqualTo(
        Score(level = null, score = BigDecimal("4.50"), isValid = true)
      )
      assertThat(predictorScores.scores[PredictorSubType.RSR_2YR_BRIEF]).isEqualTo(
        Score(HIGH, BigDecimal("0.001"), true)
      )
      assertThat(predictorScores.scores[PredictorSubType.RSR_1YR_BRIEF]).isEqualTo(
        Score(null, BigDecimal("0.002"), true)
      )
      assertThat(predictorScores.scores[PredictorSubType.RSR_1YR_EXTENDED]).isEqualTo(
        Score(null, BigDecimal("0.003"), true)
      )
      assertThat(predictorScores.scores[PredictorSubType.RSR_2YR_EXTENDED]).isEqualTo(
        Score(HIGH, BigDecimal("0.004"), true)
      )

      assertThat(predictorScores.scores[PredictorSubType.OSPI_1YR]).isEqualTo(
        Score(null, BigDecimal("0.005"), true)
      )
      assertThat(predictorScores.scores[PredictorSubType.OSPI_2YR]).isEqualTo(
        Score(HIGH, BigDecimal("0.006"), true)
      )
      assertThat(predictorScores.scores[PredictorSubType.OSPC_1YR]).isEqualTo(
        Score(null, BigDecimal("0.007"), true)
      )
      assertThat(predictorScores.scores[PredictorSubType.OSPC_2YR]).isEqualTo(
        Score(HIGH, BigDecimal("0.008"), true)
      )

      assertThat(predictorScores.scores[PredictorSubType.SNSV_1YR_BRIEF]).isEqualTo(
        Score(null, BigDecimal("0.009"), true)
      )
      assertThat(predictorScores.scores[PredictorSubType.SNSV_2YR_BRIEF]).isEqualTo(
        Score(null, BigDecimal("0.010"), true)
      )
      assertThat(predictorScores.scores[PredictorSubType.SNSV_1YR_EXTENDED]).isEqualTo(
        Score(null, BigDecimal("0.011"), true)
      )
      assertThat(predictorScores.scores[PredictorSubType.SNSV_2YR_EXTENDED]).isEqualTo(
        Score(null, BigDecimal("0.012"), true)
      )
    }

    @Test
    fun `calculate risk predictors data saves predictors when final version is true`() {
      val predictorType = PredictorType.RSR
      val algorithmVersion = "1"
      every {
        riskCalculatorService.calculatePredictorScores(predictorType, offencesAndOffencesDto, algorithmVersion)
      } returns RiskPredictorsDto(
        scoreType = ScoreType.DYNAMIC,
        algorithmVersion = "1",
        errorCount = 0,
        calculatedAt = LocalDateTime.of(2021, 7, 30, 16, 24, 25),
        type = PredictorType.RSR,
        errors = emptyList(),
        scores = mapOf(
          PredictorSubType.RSR to Score(LOW, BigDecimal("1.50"), true),
          PredictorSubType.OSPC to Score(MEDIUM, BigDecimal("2.50"), true),
          PredictorSubType.OSPI to Score(HIGH, BigDecimal("3.50"), true),
          PredictorSubType.SNSV to Score(null, BigDecimal("4.50"), true),

          PredictorSubType.RSR_1YR_BRIEF to Score(null, BigDecimal("0.001"), true),
          PredictorSubType.RSR_2YR_BRIEF to Score(HIGH, BigDecimal("0.002"), true),
          PredictorSubType.RSR_1YR_EXTENDED to Score(null, BigDecimal("0.003"), true),
          PredictorSubType.RSR_2YR_EXTENDED to Score(HIGH, BigDecimal("0.004"), true),

          PredictorSubType.OSPI_1YR to Score(null, BigDecimal("0.005"), true),
          PredictorSubType.OSPI_2YR to Score(HIGH, BigDecimal("0.006"), true),
          PredictorSubType.OSPC_1YR to Score(null, BigDecimal("0.007"), true),
          PredictorSubType.OSPC_2YR to Score(HIGH, BigDecimal("0.008"), true),

          PredictorSubType.SNSV_1YR_BRIEF to Score(null, BigDecimal("0.009"), true),
          PredictorSubType.SNSV_2YR_BRIEF to Score(null, BigDecimal("0.010"), true),
          PredictorSubType.SNSV_1YR_EXTENDED to Score(null, BigDecimal("0.011"), true),
          PredictorSubType.SNSV_2YR_EXTENDED to Score(null, BigDecimal("0.012"), true)
        )
      )

      val source = PredictorSource.ASSESSMENTS_API
      val sourceId = "sourceId"
      val offenderPredictorsHistoryEntitySlot = slot<OffenderPredictorsHistoryEntity>()

      every {
        offenderPredictorsHistoryRepository.save(
          capture(offenderPredictorsHistoryEntitySlot)
        )
      }.returns(mockk())

      riskPredictorsService.calculatePredictorScores(
        predictorType,
        offencesAndOffencesDto,
        true,
        source,
        sourceId,
        algorithmVersion
      )

      with(offenderPredictorsHistoryEntitySlot.captured) {
        assertThat(algorithmVersion).isEqualTo(algorithmVersion)
        assertThat(predictorType).isEqualTo(predictorType)
        assertThat(calculatedAt).isEqualTo(LocalDateTime.of(2021, 7, 30, 16, 24, 25))
        assertThat(crn).isEqualTo("X1345")
        assertThat(predictorTriggerSource).isEqualTo(source)
        assertThat(predictorTriggerSourceId).isEqualTo(sourceId)
        assertThat(createdBy).isEqualTo(RequestData.getUserName())
        assertThat(predictors).hasSize(16)

        with(predictors.first { predictor -> predictor.predictorSubType == PredictorSubType.RSR }) {
          assertThat(predictorScore).isEqualTo(BigDecimal("1.50"))
          assertThat(predictorLevel).isEqualTo(LOW)
        }
        with(predictors.first { predictor -> predictor.predictorSubType == PredictorSubType.OSPC }) {
          assertThat(predictorScore).isEqualTo(BigDecimal("2.50"))
          assertThat(predictorLevel).isEqualTo(MEDIUM)
        }
        with(predictors.first { predictor -> predictor.predictorSubType == PredictorSubType.OSPI }) {
          assertThat(predictorScore).isEqualTo(BigDecimal("3.50"))
          assertThat(predictorLevel).isEqualTo(HIGH)
        }
        with(predictors.first { predictor -> predictor.predictorSubType == PredictorSubType.SNSV }) {
          assertThat(predictorScore).isEqualTo(BigDecimal("4.50"))
          assertThat(predictorLevel).isNull()
        }
        with(predictors.first { predictor -> predictor.predictorSubType == PredictorSubType.RSR_1YR_BRIEF }) {
          assertThat(predictorScore).isEqualTo(BigDecimal("0.001"))
          assertThat(predictorLevel).isNull()
        }
        with(predictors.first { predictor -> predictor.predictorSubType == PredictorSubType.RSR_2YR_BRIEF }) {
          assertThat(predictorScore).isEqualTo(BigDecimal("0.002"))
          assertThat(predictorLevel).isEqualTo(HIGH)
        }
        with(predictors.first { predictor -> predictor.predictorSubType == PredictorSubType.RSR_1YR_EXTENDED }) {
          assertThat(predictorScore).isEqualTo(BigDecimal("0.003"))
          assertThat(predictorLevel).isNull()
        }
        with(predictors.first { predictor -> predictor.predictorSubType == PredictorSubType.RSR_2YR_EXTENDED }) {
          assertThat(predictorScore).isEqualTo(BigDecimal("0.004"))
          assertThat(predictorLevel).isEqualTo(HIGH)
        }
        with(predictors.first { predictor -> predictor.predictorSubType == PredictorSubType.OSPI_1YR }) {
          assertThat(predictorScore).isEqualTo(BigDecimal("0.005"))
          assertThat(predictorLevel).isNull()
        }
        with(predictors.first { predictor -> predictor.predictorSubType == PredictorSubType.OSPI_2YR }) {
          assertThat(predictorScore).isEqualTo(BigDecimal("0.006"))
          assertThat(predictorLevel).isEqualTo(HIGH)
        }
        with(predictors.first { predictor -> predictor.predictorSubType == PredictorSubType.OSPC_1YR }) {
          assertThat(predictorScore).isEqualTo(BigDecimal("0.007"))
          assertThat(predictorLevel).isNull()
        }
        with(predictors.first { predictor -> predictor.predictorSubType == PredictorSubType.OSPC_2YR }) {
          assertThat(predictorScore).isEqualTo(BigDecimal("0.008"))
          assertThat(predictorLevel).isEqualTo(HIGH)
        }
        with(predictors.first { predictor -> predictor.predictorSubType == PredictorSubType.SNSV_1YR_BRIEF }) {
          assertThat(predictorScore).isEqualTo(BigDecimal("0.009"))
          assertThat(predictorLevel).isNull()
        }
        with(predictors.first { predictor -> predictor.predictorSubType == PredictorSubType.SNSV_2YR_BRIEF }) {
          assertThat(predictorScore).isEqualTo(BigDecimal("0.010"))
          assertThat(predictorLevel).isNull()
        }
        with(predictors.first { predictor -> predictor.predictorSubType == PredictorSubType.SNSV_1YR_EXTENDED }) {
          assertThat(predictorScore).isEqualTo(BigDecimal("0.011"))
          assertThat(predictorLevel).isNull()
        }
        with(predictors.first { predictor -> predictor.predictorSubType == PredictorSubType.SNSV_2YR_EXTENDED }) {
          assertThat(predictorScore).isEqualTo(BigDecimal("0.012"))
          assertThat(predictorLevel).isNull()
        }
      }
    }

    @Test
    fun `calculate risk predictors final version throws IncorrectInputParametersException if crn is null`() {
      val predictorType = PredictorType.RSR

      assertThrows<IncorrectInputParametersException> {
        riskPredictorsService.calculatePredictorScores(
          predictorType,
          offencesAndOffencesDto.copy(crn = null),
          true,
          PredictorSource.ASSESSMENTS_API,
          "sourceId"
        )
      }
    }
  }

  companion object {
    val sourceAnswersJson =
      """{"gender":"MALE","dob":"2001-01-01","assessment_date":"2021-01-01T00:00:00","offence_code":"138","offence_subcode":"00","date_first_sanction":"2020-01-01","total_sanctions":10,"total_violent_offences":8,"date_current_conviction":"2020-12-18","any_sexual_offences":true,"current_sexual_offence":true,"current_offence_victim_stranger":true,"most_recent_sexual_offence_date":"2020-12-11","total_sexual_offences_adult":5,"total_sexual_offences_child":5,"total_sexual_offences_child_image":2,"total_non_contact_sexual_offences":2,"earliest_release_date":"2021-11-01","completed_interview":true,"suitable_accommodation":"MISSING","unemployed_on_release":"NOT_AVAILABLE_FOR_WORK","current_relationship_with_partner":"SIGNIFICANT_PROBLEMS","evidence_domestic_violence":true,"perpetrator_domestic_violence":true,"use_of_alcohol":"SIGNIFICANT_PROBLEMS","binge_drinking":"SIGNIFICANT_PROBLEMS","impulsivity_issues":"SOME_PROBLEMS","temper_control_issues":"SIGNIFICANT_PROBLEMS","pro_criminal_attitudes":"SOME_PROBLEMS","previous_murder_attempt":true,"previous_wounding":true,"previous_aggravated_burglary":true,"previous_arson":true,"previous_criminal_damage":true,"previous_kidnapping":true,"previous_possession_firearm":true,"previous_robbery":true,"previous_offence_weapon":true,"current_possession_firearm":true,"current_offence_weapon":true}  """.trimIndent()
  }
}
