package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import ai.onnxruntime.OrtEnvironment
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.CurrentOffenceDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.CurrentOffencesDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.DynamicScoringOffencesDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.EmploymentType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.Gender
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OffenderAndOffencesDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PredictorSubType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PredictorType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PreviousOffencesDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ProblemsLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreType
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
@DisplayName("ONNX Risk Predictor Tests")
class OnnxCalculatorServiceImplTest {

  private val ortEnvironment = OrtEnvironment.getEnvironment()

  private val briefOnnxFile = "./src/test/resources/onnx/const_rsr_brief.onnx"
  private val extendedOnnxFile = "./src/test/resources/onnx/const_rsr_extended.onnx"
  private val ortSessionBrief = ortEnvironment.createSession(briefOnnxFile)
  private val ortSessionExtended = ortEnvironment.createSession(extendedOnnxFile)
  private val briefOnnxCalculatorService = OnnxCalculatorServiceImpl(ortEnvironment, ortSessionBrief)
  private val extendedOnnxCalculatorService = OnnxCalculatorServiceImpl(ortEnvironment, ortSessionExtended)

  @Nested
  @DisplayName("RSR outputs")
  inner class ValidateRSRScores {

    @Test
    fun `should return extended scores when called with extended parameters`() {
      val result =
        extendedOnnxCalculatorService.calculatePredictorScores(PredictorType.RSR, getExtendedInputParameters())

      assertThat(result.scoreType).isEqualTo(ScoreType.DYNAMIC)
      assertThat(result.type).isEqualTo(PredictorType.RSR)
      assertThat(result.errorCount).isEqualTo(0)
      assertThat(result.errors).isEmpty()

      assertThat(result.scores[PredictorSubType.RSR]?.score).isEqualByComparingTo(BigDecimal.valueOf(0.2943015))
      assertThat(result.scores[PredictorSubType.RSR]?.level).isEqualTo(ScoreLevel.HIGH)
      assertThat(result.scores[PredictorSubType.OSPC]?.score).isEqualByComparingTo(BigDecimal.valueOf(0.22851883))
      assertThat(result.scores[PredictorSubType.OSPC]?.level).isEqualTo(ScoreLevel.VERY_HIGH)
      assertThat(result.scores[PredictorSubType.OSPI]?.score).isEqualByComparingTo(BigDecimal.valueOf(0.057949))
      assertThat(result.scores[PredictorSubType.OSPI]?.level).isNull()
      assertThat(result.scores[PredictorSubType.SNSV]?.score).isEqualByComparingTo(BigDecimal.valueOf(0.007833679))
      assertThat(result.scores[PredictorSubType.SNSV]?.level).isNull()

      assertThat(result.scores[PredictorSubType.RSR_1YR_BRIEF]?.score).isEqualByComparingTo(
        BigDecimal.valueOf(
          0.18834394
        )
      )
      assertThat(result.scores[PredictorSubType.RSR_1YR_BRIEF]?.level).isNull()
      assertThat(result.scores[PredictorSubType.RSR_2YR_BRIEF]?.score).isEqualByComparingTo(
        BigDecimal.valueOf(
          0.30561092
        )
      )
      assertThat(result.scores[PredictorSubType.RSR_2YR_BRIEF]?.level).isEqualTo(ScoreLevel.HIGH)

      assertThat(result.scores[PredictorSubType.RSR_1YR_EXTENDED]?.score).isEqualByComparingTo(
        BigDecimal.valueOf(
          0.18209533
        )
      )
      assertThat(result.scores[PredictorSubType.RSR_1YR_EXTENDED]?.level).isNull()
      assertThat(result.scores[PredictorSubType.RSR_2YR_EXTENDED]?.score).isEqualByComparingTo(
        BigDecimal.valueOf(
          0.2943015
        )
      )
      assertThat(result.scores[PredictorSubType.RSR_2YR_EXTENDED]?.level).isEqualTo(ScoreLevel.HIGH)

      assertThat(result.scores[PredictorSubType.OSPC_1YR]?.score).isEqualByComparingTo(BigDecimal.valueOf(0.13972345))
      assertThat(result.scores[PredictorSubType.OSPC_1YR]?.level).isNull()
      assertThat(result.scores[PredictorSubType.OSPC_2YR]?.score).isEqualByComparingTo(BigDecimal.valueOf(0.22851883))
      assertThat(result.scores[PredictorSubType.OSPC_2YR]?.level).isEqualTo(ScoreLevel.VERY_HIGH)

      assertThat(result.scores[PredictorSubType.OSPI_1YR]?.score).isEqualByComparingTo(BigDecimal.valueOf(0.038087))
      assertThat(result.scores[PredictorSubType.OSPI_1YR]?.level).isNull()
      assertThat(result.scores[PredictorSubType.OSPI_2YR]?.score).isEqualByComparingTo(BigDecimal.valueOf(0.057949))
      assertThat(result.scores[PredictorSubType.OSPI_2YR]?.level).isNull()

      assertThat(result.scores[PredictorSubType.SNSV_1YR_BRIEF]?.score).isEqualByComparingTo(
        BigDecimal.valueOf(
          0.010533482
        )
      )
      assertThat(result.scores[PredictorSubType.SNSV_1YR_BRIEF]?.level).isNull()
      assertThat(result.scores[PredictorSubType.SNSV_2YR_BRIEF]?.score).isEqualByComparingTo(
        BigDecimal.valueOf(
          0.019143075
        )
      )
      assertThat(result.scores[PredictorSubType.SNSV_2YR_BRIEF]?.level).isNull()

      assertThat(result.scores[PredictorSubType.SNSV_1YR_EXTENDED]?.score).isEqualByComparingTo(
        BigDecimal.valueOf(
          0.004284886
        )
      )
      assertThat(result.scores[PredictorSubType.SNSV_1YR_EXTENDED]?.level).isNull()
      assertThat(result.scores[PredictorSubType.SNSV_2YR_EXTENDED]?.score).isEqualByComparingTo(
        BigDecimal.valueOf(
          0.007833679
        )
      )
      assertThat(result.scores[PredictorSubType.SNSV_2YR_EXTENDED]?.level).isNull()
    }

    @Test
    fun `should return brief score when called without extended parameters`() {
      val result = briefOnnxCalculatorService.calculatePredictorScores(PredictorType.RSR, getBriefInputParameters())

      assertThat(result.scoreType).isEqualTo(ScoreType.STATIC)
      assertThat(result.type).isEqualTo(PredictorType.RSR)
      assertThat(result.errorCount).isEqualTo(0)
      assertThat(result.errors).isEmpty()

      assertThat(result.scores[PredictorSubType.RSR]?.score).isEqualByComparingTo(BigDecimal.valueOf(0.30561092))
      assertThat(result.scores[PredictorSubType.RSR]?.level).isEqualTo(ScoreLevel.HIGH)
//      assertThat(result.scores[PredictorSubType.OSPC]?.score).isNull()
//      assertThat(result.scores[PredictorSubType.OSPC]?.level).isNull()
//      assertThat(result.scores[PredictorSubType.OSPI]?.score).isNull()
//      assertThat(result.scores[PredictorSubType.OSPI]?.level).isNull()
//      assertThat(result.scores[PredictorSubType.SNSV]?.score).isNull()
//      assertThat(result.scores[PredictorSubType.SNSV]?.level).isNull()

      assertThat(result.scores[PredictorSubType.RSR_1YR_BRIEF]?.score).isEqualByComparingTo(
        BigDecimal.valueOf(
          0.18834394
        )
      )
      assertThat(result.scores[PredictorSubType.RSR_1YR_BRIEF]?.level).isNull()
      assertThat(result.scores[PredictorSubType.RSR_2YR_BRIEF]?.score).isEqualByComparingTo(
        BigDecimal.valueOf(
          0.30561092
        )
      )
      assertThat(result.scores[PredictorSubType.RSR_2YR_BRIEF]?.level).isEqualTo(ScoreLevel.HIGH)

//      assertThat(result.scores[PredictorSubType.RSR_1YR_EXTENDED]?.score).isNull()
//      assertThat(result.scores[PredictorSubType.RSR_1YR_EXTENDED]?.level).isNull()
//      assertThat(result.scores[PredictorSubType.RSR_2YR_EXTENDED]?.score).isNull()
//      assertThat(result.scores[PredictorSubType.RSR_2YR_EXTENDED]?.level).isNull()
//
//      assertThat(result.scores[PredictorSubType.OSPC_1YR]?.score).isNull()
//      assertThat(result.scores[PredictorSubType.OSPC_1YR]?.level).isNull()
//      assertThat(result.scores[PredictorSubType.OSPC_2YR]?.score).isNull()
//      assertThat(result.scores[PredictorSubType.OSPC_2YR]?.level).isNull()
//
//      assertThat(result.scores[PredictorSubType.OSPI_1YR]?.score).isNull()
//      assertThat(result.scores[PredictorSubType.OSPI_1YR]?.level).isNull()
//      assertThat(result.scores[PredictorSubType.OSPI_2YR]?.score).isNull()
//      assertThat(result.scores[PredictorSubType.OSPI_2YR]?.level).isNull()
//
//      assertThat(result.scores[PredictorSubType.SNSV_1YR_BRIEF]?.score).isNull()
//      assertThat(result.scores[PredictorSubType.SNSV_1YR_BRIEF]?.level).isNull()
//      assertThat(result.scores[PredictorSubType.SNSV_2YR_BRIEF]?.score).isNull()
//      assertThat(result.scores[PredictorSubType.SNSV_2YR_BRIEF]?.level).isNull()
//
//      assertThat(result.scores[PredictorSubType.SNSV_1YR_EXTENDED]?.score).isNull()
//      assertThat(result.scores[PredictorSubType.SNSV_1YR_EXTENDED]?.level).isNull()
//      assertThat(result.scores[PredictorSubType.SNSV_2YR_EXTENDED]?.score).isNull()
//      assertThat(result.scores[PredictorSubType.SNSV_2YR_EXTENDED]?.level).isNull()
    }
  }

  @Nested
  @DisplayName("ONNX Input Validation")
  inner class OnnxInputValidation {

    @Test
    fun `should return validation error when interview is true but dynamic scores not provided`() {
      val inputParameters = getExtendedInputParameters().copy(hasCompletedInterview = true, dynamicScoringOffences = null)
      val result = extendedOnnxCalculatorService.calculatePredictorScores(PredictorType.RSR, inputParameters)

      val expectedErrors = listOf(
        "Is the individual living in suitable accommodation is required",
        "Is the person unemployed or will be unemployed upon release is required",
        "What is the person's current relationship with their partner is required",
        "Is there evidence that the individual is a perpetrator of domestic abuse is required",
        "Is the person's current use of alcohol a problem is required",
        "Is there evidence of binge drinking or excessive use of alcohol in the last 6 months is required",
        "Is impulsivity a problem for the individual is required",
        "Is temper control a problem for the individual is required",
        "Does the individual have pro-criminal attitudes is required",
        "Possession of a firearm with intent to endanger life or resist arrest is required",
        "Any other offence involving possession and/or use of weapons is required",
        "Murder/attempted murder/threat or conspiracy to murder/manslaughter is required",
        "Wounding/GBH (Sections 18/20 Offences Against the Person Act 1861) is required",
        "Aggravated burglary is required",
        "Arson is required",
        "Criminal damage with intent to endanger life is required",
        "Kidnapping/false imprisonment",
        "Possession of a firearm with intent to endanger life or resist arrest is required",
        "Robbery is required",
        "Any other offence involving possession and/or use of weapons is required"
      )

      assertThat(result.scoreType).isNull()
      assertThat(result.type).isEqualTo(PredictorType.RSR)
      assertThat(result.scores).isEmpty()
      assertThat(result.errorCount).isEqualTo(20)
      assertThat(result.errors).containsExactlyInAnyOrderElementsOf(expectedErrors)
    }

    @Test
    fun `should return validation error when date of conviction is in the future`() {
      val inputParameters = getExtendedInputParameters().copy(dateOfCurrentConviction = LocalDate.now().plusDays(1))
      val result = extendedOnnxCalculatorService.calculatePredictorScores(PredictorType.RSR, inputParameters)

      val expectedErrors = listOf("Date of current conviction cannot be in the future")

      assertThat(result.scoreType).isNull()
      assertThat(result.type).isEqualTo(PredictorType.RSR)
      assertThat(result.scores).isEmpty()
      assertThat(result.errorCount).isEqualTo(1)
      assertThat(result.errors).containsExactlyInAnyOrderElementsOf(expectedErrors)
    }

    @Test
    fun `should return validation error when date of conviction is before the first conviction`() {
      val inputParameters = getExtendedInputParameters()
        .copy(
          dateOfCurrentConviction = LocalDate.of(2019, 1, 1),
          dateOfFirstSanction = LocalDate.of(2019, 1, 2)
        )

      val result = extendedOnnxCalculatorService.calculatePredictorScores(PredictorType.RSR, inputParameters)

      val expectedErrors = listOf("Date of current conviction cannot be before the date of first conviction")

      assertThat(result.scoreType).isNull()
      assertThat(result.type).isEqualTo(PredictorType.RSR)
      assertThat(result.scores).isEmpty()
      assertThat(result.errorCount).isEqualTo(1)
      assertThat(result.errors).containsExactlyInAnyOrderElementsOf(expectedErrors)
    }

    @Test
    fun `should return validation error when date of first sanction is in the future`() {
      val inputParameters = getExtendedInputParameters()
        .copy(
          dateOfFirstSanction = LocalDate.now().plusDays(1)
        )

      val result = extendedOnnxCalculatorService.calculatePredictorScores(PredictorType.RSR, inputParameters)

      val expectedErrors = listOf("Date of first sanction cannot be in the future")

      assertThat(result.scoreType).isNull()
      assertThat(result.type).isEqualTo(PredictorType.RSR)
      assertThat(result.scores).isEmpty()
      assertThat(result.errors).containsAll(expectedErrors)
    }

    @Test
    fun `should return validation error when offenders age is under 8 on date of first sanction`() {
      val inputParameters = getExtendedInputParameters()
        .copy(
          dob = LocalDate.of(2010, 1, 1),
          dateOfFirstSanction = LocalDate.of(2017, 12, 31)
        )

      val result = extendedOnnxCalculatorService.calculatePredictorScores(PredictorType.RSR, inputParameters)

      val expectedErrors = listOf("The individual must be aged 8 or older on the date of first sanction")

      assertThat(result.scoreType).isNull()
      assertThat(result.type).isEqualTo(PredictorType.RSR)
      assertThat(result.scores).isEmpty()
      assertThat(result.errorCount).isEqualTo(1)
      assertThat(result.errors).containsExactlyInAnyOrderElementsOf(expectedErrors)
    }

    @Test
    fun `should return validation error when earliest release date is before offenders DOB`() {
      val inputParameters = getExtendedInputParameters()
        .copy(
          dob = LocalDate.of(2010, 1, 2),
          earliestReleaseDate = LocalDate.of(2010, 1, 1)
        )

      val result = extendedOnnxCalculatorService.calculatePredictorScores(PredictorType.RSR, inputParameters)

      val expectedErrors = listOf("Date of earliest possible release from custody must be later than the individual’s date of birth")

      assertThat(result.scoreType).isNull()
      assertThat(result.type).isEqualTo(PredictorType.RSR)
      assertThat(result.scores).isEmpty()
      assertThat(result.errorCount).isEqualTo(1)
      assertThat(result.errors).containsExactlyInAnyOrderElementsOf(expectedErrors)
    }

    @Test
    fun `should return validation error when offenders age is over 110 at earliest release date`() {
      val inputParameters = getExtendedInputParameters()
        .copy(
          dob = LocalDate.of(1900, 1, 1),
          earliestReleaseDate = LocalDate.of(2011, 1, 1)
        )

      val result = extendedOnnxCalculatorService.calculatePredictorScores(PredictorType.RSR, inputParameters)

      val expectedErrors = listOf("The individual must be aged 110 or younger on commencement")

      assertThat(result.scoreType).isNull()
      assertThat(result.type).isEqualTo(PredictorType.RSR)
      assertThat(result.scores).isEmpty()
      assertThat(result.errorCount).isEqualTo(1)
      assertThat(result.errors).containsExactlyInAnyOrderElementsOf(expectedErrors)
    }

    @Test
    fun `should return validation error when total offences is 0`() {
      val inputParameters = getExtendedInputParameters()
        .copy(totalOffences = 0, totalViolentOffences = 0)

      val result = extendedOnnxCalculatorService.calculatePredictorScores(PredictorType.RSR, inputParameters)

      val expectedErrors = listOf("Total offences must be at least 1 including the current offence")

      assertThat(result.scoreType).isNull()
      assertThat(result.type).isEqualTo(PredictorType.RSR)
      assertThat(result.scores).isEmpty()
      assertThat(result.errorCount).isEqualTo(1)
      assertThat(result.errors).containsExactlyInAnyOrderElementsOf(expectedErrors)
    }

    @Test
    fun `should return validation error when violeent offences is greater than total offences`() {
      val inputParameters = getExtendedInputParameters()
        .copy(totalOffences = 1, totalViolentOffences = 2)

      val result = extendedOnnxCalculatorService.calculatePredictorScores(PredictorType.RSR, inputParameters)

      val expectedErrors = listOf("Violent offences must less than equal to the total number of offences")

      assertThat(result.scoreType).isNull()
      assertThat(result.type).isEqualTo(PredictorType.RSR)
      assertThat(result.scores).isEmpty()
      assertThat(result.errorCount).isEqualTo(1)
      assertThat(result.errors).containsExactlyInAnyOrderElementsOf(expectedErrors)
    }

    @Test
    fun `should return validation errors when any sexual offence details are missing`() {
      val inputParameters = getExtendedInputParameters()
        .copy(
          hasAnySexualOffences = true,
          isCurrentSexualOffence = null,
          mostRecentSexualOffenceDate = null,
          isCurrentOffenceVictimStranger = null,
          totalNonContactSexualOffences = null,
          totalSexualOffencesInvolvingAnAdult = null,
          totalSexualOffencesInvolvingAChild = null,
          totalSexualOffencesInvolvingChildImages = null,
        )

      val result = extendedOnnxCalculatorService.calculatePredictorScores(PredictorType.RSR, inputParameters)

      val expectedErrors = listOf(
        "Has current sexual offence is required",
        "Date of most recent sexual offence is required",
        "Does the current offence involve a victim who was a stranger is required",
        "Number of previous or current sanctions involving contact adult sexual or sexually motivated offences is required",
        "Number of previous or current sanctions involving contact child sexual or sexually motivated offences is required",
        "Number of previous or current sanctions involving indecent child image sexual or sexually motivated offences is required",
        "Number of previous or current sanctions involving other non-contact sexual or sexually motivated offences is required",
        "At least one sexual offence is required"
      )

      assertThat(result.scoreType).isNull()
      assertThat(result.type).isEqualTo(PredictorType.RSR)
      assertThat(result.scores).isEmpty()
      assertThat(result.errorCount).isEqualTo(8)
      assertThat(result.errors).containsExactlyInAnyOrderElementsOf(expectedErrors)
    }

    @Test
    fun `should return validation error if at least one sexual offence is not provided`() {
      val inputParameters = getExtendedInputParameters()
        .copy(
          hasAnySexualOffences = true,
          totalNonContactSexualOffences = 0,
          totalSexualOffencesInvolvingAnAdult = 0,
          totalSexualOffencesInvolvingAChild = 0,
          totalSexualOffencesInvolvingChildImages = 0,
        )

      val result = extendedOnnxCalculatorService.calculatePredictorScores(PredictorType.RSR, inputParameters)

      val expectedErrors = listOf("At least one sexual offence is required")

      assertThat(result.scoreType).isNull()
      assertThat(result.type).isEqualTo(PredictorType.RSR)
      assertThat(result.scores).isEmpty()
      assertThat(result.errorCount).isEqualTo(1)
      assertThat(result.errors).containsExactlyInAnyOrderElementsOf(expectedErrors)
    }

    @Test
    fun `should return validation error when date of most recent sexual offence is in the future`() {
      val inputParameters = getExtendedInputParameters()
        .copy(
          mostRecentSexualOffenceDate = LocalDate.now().plusDays(1)
        )

      val result = extendedOnnxCalculatorService.calculatePredictorScores(PredictorType.RSR, inputParameters)

      val expectedErrors = listOf("Date of most recent sexual offence cannot be in the future")

      assertThat(result.scoreType).isNull()
      assertThat(result.type).isEqualTo(PredictorType.RSR)
      assertThat(result.scores).isEmpty()
      assertThat(result.errors).containsAll(expectedErrors)
    }

    @Test
    fun `should return validation error when date of most recent sexual offence is before offenders DOB`() {
      val inputParameters = getExtendedInputParameters()
        .copy(
          dob = LocalDate.of(2000, 1, 2),
          mostRecentSexualOffenceDate = LocalDate.of(2000, 1, 1)
        )

      val result = extendedOnnxCalculatorService.calculatePredictorScores(PredictorType.RSR, inputParameters)

      val expectedErrors = listOf("Date of most recent sexual offence must be later than the individual’s date of birth")

      assertThat(result.scoreType).isNull()
      assertThat(result.type).isEqualTo(PredictorType.RSR)
      assertThat(result.scores).isEmpty()
      assertThat(result.errors).containsAll(expectedErrors)
    }
  }

  private fun getExtendedInputParameters(): OffenderAndOffencesDto {

    return OffenderAndOffencesDto(
      crn = "X1345",
      gender = Gender.MALE,
      dob = LocalDate.of(2021, 1, 1).minusYears(20),
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
  }

  private fun getBriefInputParameters(): OffenderAndOffencesDto {

    return OffenderAndOffencesDto(
      crn = "X1345",
      gender = Gender.MALE,
      dob = LocalDate.of(2021, 1, 1).minusYears(20),
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
      hasCompletedInterview = false
    )
  }
}
