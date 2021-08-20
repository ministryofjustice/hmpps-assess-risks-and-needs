package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.MDC
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.CurrentOffence
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.CurrentOffences
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.DynamicScoringOffences
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.EmploymentType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.Gender
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OffenderAndOffencesDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PredictorType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PreviousOffences
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ProblemsLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.Score
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.config.RequestData
import uk.gov.justice.digital.hmpps.assessrisksandneeds.jpa.entities.OffenderPredictorsHistoryEntity
import uk.gov.justice.digital.hmpps.assessrisksandneeds.jpa.respositories.OffenderPredictorsHistoryRepository
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.AssessmentApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.OasysRSRPredictorsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.PredictorCalculationError
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@ExtendWith(MockKExtension::class)
@DisplayName("Risk Predictors Service Tests")
class RiskPredictorServiceTest {

  private val assessmentApiClient: AssessmentApiRestClient = mockk()
  private val offenderPredictorsHistoryRepository: OffenderPredictorsHistoryRepository = mockk()

  private val riskPredictorsService = RiskPredictorService(assessmentApiClient, offenderPredictorsHistoryRepository)

  private val offencesAndOffencesDto = OffenderAndOffencesDto(
    crn = "X1345",
    gender = Gender.MALE,
    dob = LocalDate.of(2000, 1, 1).minusYears(20),
    assessmentDate = LocalDateTime.of(2021, 1, 1, 0, 0, 0),
    currentOffence = CurrentOffence("138", "00"),
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
    dynamicScoringOffences = DynamicScoringOffences(
      hasSuitableAccommodation = ProblemsLevel.MISSING,
      employment = EmploymentType.NOT_AVAILABLE_FOR_WORK,
      currentRelationshipWithPartner = ProblemsLevel.SIGNIFICANT_PROBLEMS,
      evidenceOfDomesticViolence = true,
      isVictim = true,
      isPerpetrator = true,
      alcoholUseIssues = ProblemsLevel.SIGNIFICANT_PROBLEMS,
      bingeDrinkingIssues = ProblemsLevel.SIGNIFICANT_PROBLEMS,
      impulsivityIssues = ProblemsLevel.SOME_PROBLEMS,
      temperControlIssues = ProblemsLevel.SIGNIFICANT_PROBLEMS,
      proCriminalAttitudes = ProblemsLevel.SOME_PROBLEMS,
      previousOffences = PreviousOffences(
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
      currentOffences = CurrentOffences(
        firearmPossession = true,
        offencesWithWeapon = true
      )
    )
  )

  @BeforeEach
  fun setup() {
    MDC.put(RequestData.USER_NAME_HEADER, "User name")
  }

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
        "source",
        "sourceId"
      )
    }
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
      "source",
      "sourceId"
    )

    assertThat(predictorScores.calculatedAt).isEqualTo(LocalDateTime.of(2021, 7, 30, 16, 24, 25))
    assertThat(predictorScores.type).isEqualTo(PredictorType.RSR)
    assertThat(predictorScores.scoreType).isEqualTo(ScoreType.STATIC)
    assertThat(predictorScores.scores["RSR"]).isEqualTo(
      Score(
        level = ScoreLevel.HIGH, score = BigDecimal("11.34"), isValid = true
      )
    )
    assertThat(predictorScores.scores["OSPC"]).isEqualTo(
      Score(
        level = ScoreLevel.NOT_APPLICABLE, score = BigDecimal("0"), isValid = false
      )
    )
    assertThat(predictorScores.scores["OSPI"]).isEqualTo(
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
      "source",
      "sourceId"
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

    val source = "source"
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
      algorithmVersion = "3",
      calculatedAt = LocalDateTime.of(2021, 7, 30, 16, 24, 25),
      crn = "X1345",
      predictorTriggerSource = source,
      predictorTriggerSourceId = sourceId,
      createdBy = RequestData.getUserName()
    )

    riskPredictorsService.calculatePredictorScores(
      predictorType,
      offencesAndOffencesDto,
      true,
      source,
      sourceId
    )

    with(offenderPredictorsHistoryEntitySlot.captured) {
      assertThat(algorithmVersion).isEqualTo("3")
      assertThat(predictorType).isEqualTo(predictorType)
      assertThat(calculatedAt).isEqualTo(LocalDateTime.of(2021, 7, 30, 16, 24, 25))
      assertThat(crn).isEqualTo("X1345")
      assertThat(predictorTriggerSource).isEqualTo(source)
      assertThat(predictorTriggerSourceId).isEqualTo(sourceId)
      assertThat(createdBy).isEqualTo(RequestData.getUserName())
    }
  }
}
