package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
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
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.AssessmentApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.OasysRSRPredictorsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.PredictorCalculationError
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
@DisplayName("Risk Predictors Service Tests")
class RiskPredictorServiceTest {

  private val assessmentApiClient: AssessmentApiRestClient = mockk()

  private val riskPredictorsService = RiskPredictorService(assessmentApiClient)

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
    totalNonSexualOffences = 2,
    earliestReleaseDate = LocalDate.of(2021, 1, 1).plusMonths(10),
    hasCompletedInterview = true,
    dynamicScoringOffences = DynamicScoringOffences(
      hasSuitableAccommodation = ProblemsLevel.MISSING,
      employment = EmploymentType.NOT_AVAILABLE_FOR_WORK,
      currentRelationshipWithPartner = ProblemsLevel.SIGNIFICANT_PROBLEMS,
      evidenceOfDomesticViolence = true,
      isAVictim = true,
      isAPerpetrator = true,
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

  @Test
  fun `get risk predictors data throws PredictorCalculationError if calculation is null`() {
    val predictorType = PredictorType.RSR
    every {
      assessmentApiClient.calculatePredictorTypeScoring(predictorType, offencesAndOffencesDto)
    } returns null

    assertThrows<PredictorCalculationError> {
      riskPredictorsService.getPredictorScores(
        predictorType,
        offencesAndOffencesDto
      )
    }
  }

  @Test
  fun `get risk predictors data returns oasys predictors`() {
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

    val predictorScores = riskPredictorsService.getPredictorScores(
      predictorType,
      offencesAndOffencesDto
    )

    assertThat(predictorScores.calculatedAt).isEqualTo(LocalDateTime.of(2021, 7, 30, 16, 24, 25))
    assertThat(predictorScores.type).isEqualTo(PredictorType.RSR)
    assertThat(predictorScores.scoreType).isEqualTo(ScoreType.STATIC)
    assertThat(predictorScores.rsrScore).isEqualTo(
      Score(
        level = ScoreLevel.HIGH, score = BigDecimal("11.34"), isValid = true
      )
    )
    assertThat(predictorScores.ospcScore).isEqualTo(
      Score(
        level = ScoreLevel.NOT_APPLICABLE, score = BigDecimal("0"), isValid = false
      )
    )
    assertThat(predictorScores.ospiScore).isEqualTo(
      Score(
        level = ScoreLevel.NOT_APPLICABLE, score = BigDecimal("0"), isValid = false
      )
    )
  }

  @Test
  fun `get risk predictors data throws PredictorCalculationError if calculation returns errors`() {
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
      errorCount = 1,
      errorMessage = "error error error",
      calculationDateAndTime = LocalDateTime.now()
    )

    val exception = assertThrows<PredictorCalculationError> {
      riskPredictorsService.getPredictorScores(
        predictorType,
        offencesAndOffencesDto
      )
    }
    assertThat(exception.message).isEqualTo("Oasys Predictor Calculation failed for offender with CRN ${offencesAndOffencesDto.crn} and $predictorType - error error error")
  }
}
