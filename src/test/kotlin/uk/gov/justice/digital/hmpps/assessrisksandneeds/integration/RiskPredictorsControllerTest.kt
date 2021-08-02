package uk.gov.justice.digital.hmpps.assessrisksandneeds.integration

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.CurrentOffence
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.DynamicScoringOffences
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.EmploymentType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.Gender
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OffenderAndOffencesDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PredictorType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PreviousOffences
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ProblemsLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RiskPredictorsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.Score
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreType
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@AutoConfigureWebTestClient
@DisplayName("Risk Predictors Tests")
class RiskPredictorsControllerTest() : IntegrationTestBase() {

  @Test
  fun `calculate rsr predictors returns rsr scoring`() {

    val requestBody = OffenderAndOffencesDto(
      crn = "X1345",
      gender = Gender.MALE,
      dob = LocalDate.of(2021, 1, 1).minusYears(20),
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
        committedOffenceUsingWeapon = true,
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
        )
      )
    )

    webTestClient.post().uri("/risks/predictors/RSR")
      .header("Content-Type", "application/json")
      .headers(setAuthorisation(user = "Gary C", roles = listOf("ROLE_PROBATION")))
      .bodyValue(requestBody)
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody<RiskPredictorsDto>()
      .consumeWith {
        Assertions.assertThat(it.responseBody).isEqualTo(
          RiskPredictorsDto(
            algorithmVersion = 3,
            type = PredictorType.RSR,
            scoreType = ScoreType.STATIC,
            rsrScore = Score(
              level = ScoreLevel.HIGH, score = BigDecimal("11.34"), isValid = true
            ),
            ospcScore = Score(
              level = ScoreLevel.NOT_APPLICABLE, score = BigDecimal("0"), isValid = false
            ),
            ospiScore = Score(
              level = ScoreLevel.NOT_APPLICABLE, score = BigDecimal("0"), isValid = false
            ),
          )
        )
      }
  }
}
