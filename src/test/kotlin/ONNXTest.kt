import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.CurrentOffence
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.CurrentOffences
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.DynamicScoringOffences
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.EmploymentType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.Gender
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OffenderAndOffencesDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PreviousOffences
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ProblemsLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.OnnxPredictorService
import java.time.LocalDate
import java.time.LocalDateTime

class ONNXTest {

  companion object {
    // Test method to run the ONNX predictor
    @JvmStatic
    fun main(args: Array<String>) {
      val offencesAndOffencesDto = OffenderAndOffencesDto(
        crn = "X1345",
        gender = Gender.MALE,
        dob = LocalDate.of(2000, 11, 11).minusYears(20),
        assessmentDate = LocalDateTime.of(2021, 11, 11, 0, 0, 0),
        currentOffence = CurrentOffence("001", "01"),
        dateOfFirstSanction = LocalDate.of(2000, 11, 11).minusYears(1),
        totalOffences = 50,
        totalViolentOffences = 8,
        dateOfCurrentConviction = LocalDate.of(2020, 11, 1).minusWeeks(2),
        hasAnySexualOffences = true,
        isCurrentSexualOffence = true,
        isCurrentOffenceVictimStranger = true,
        mostRecentSexualOffenceDate = LocalDate.of(2020, 10, 11).minusWeeks(3),
        totalSexualOffencesInvolvingAnAdult = 5,
        totalSexualOffencesInvolvingAChild = 3,
        totalSexualOffencesInvolvingChildImages = 2,
        totalNonContactSexualOffences = 2,
        earliestReleaseDate = LocalDate.of(2022, 11, 11).plusMonths(10),
        hasCompletedInterview = true,
        dynamicScoringOffences = DynamicScoringOffences(
          hasSuitableAccommodation = ProblemsLevel.SIGNIFICANT_PROBLEMS,
          employment = EmploymentType.NOT_AVAILABLE_FOR_WORK,
          currentRelationshipWithPartner = ProblemsLevel.SIGNIFICANT_PROBLEMS,
          evidenceOfDomesticViolence = true,
          isPerpetrator = true,
          alcoholUseIssues = ProblemsLevel.SIGNIFICANT_PROBLEMS,
          bingeDrinkingIssues = ProblemsLevel.SIGNIFICANT_PROBLEMS,
          impulsivityIssues = ProblemsLevel.SIGNIFICANT_PROBLEMS,
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

      OnnxPredictorService("/tmp/onnx/rsr.onnx").calculatePredictorScores(
        offencesAndOffencesDto
      )
    }
  }
}
