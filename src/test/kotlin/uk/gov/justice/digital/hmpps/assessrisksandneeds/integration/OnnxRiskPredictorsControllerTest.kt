package uk.gov.justice.digital.hmpps.assessrisksandneeds.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.CurrentOffenceDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.CurrentOffencesDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.DynamicScoringOffencesDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.EmploymentType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ErrorResponse
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.Gender
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OffenderAndOffencesDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PreviousOffencesDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ProblemsLevel
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RsrPredictorDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ScoreLevel
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.stream.Stream

@AutoConfigureWebTestClient(timeout = "360000000")
@DisplayName("ONNX Risk Predictors Tests")
@ActiveProfiles("test", "onnx-rsr", inheritProfiles = false)
// Use mock ONNX file const_rsr_extended.onnx
@TestPropertySource(properties = ["onnx-predictors.onnx-path=classpath:/onnx/rsr_v0.0.0_const_extended.onnx"])
class OnnxRiskPredictorsControllerTest : IntegrationTestBase() {

  companion object {
    @JvmStatic
    fun currentOffenceDtos(): Stream<Arguments> {
      return return Stream.of(
        Arguments.of(CurrentOffenceDto("138", ""), "offenceSubcode is a mandatory field and cannot be null or empty"),
        Arguments.of(CurrentOffenceDto("", "00"), "offenceCode is a mandatory field and cannot be null or empty"),
      )
    }
  }

  @Test
  fun `store ONNX scores in predictor history`() {

    val crn = "X234567"
    val requestBody = createOffenderAndOffencesDto(crn, CurrentOffenceDto("138", "00"))

    webTestClient.post()
      .uri("/risks/predictors/RSR?final=true&source=ASSESSMENTS_API&sourceId=90f2b674-ae1c-488d-8b85-0251708ef6b6")
      .header("Content-Type", "application/json")
      .headers(setAuthorisation(user = "Gary C", roles = listOf("ROLE_PROBATION")))
      .bodyValue(requestBody)
      .exchange()
      .expectStatus().isOk

    val rsrHistory = webTestClient.get()
      .uri("/risks/crn/$crn/predictors/rsr/history")
      .header("Content-Type", "application/json")
      .headers(setAuthorisation(user = "Gary C", roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody<List<RsrPredictorDto>>()
      .returnResult().responseBody

    assertThat(rsrHistory).hasSize(1)

    with(rsrHistory[0]) {
      assertThat(rsrPercentageScore).isEqualTo("29.43")
      assertThat(rsrScoreLevel).isEqualTo(ScoreLevel.HIGH)
      assertThat(ospcPercentageScore).isEqualTo("22.85")
      assertThat(ospcScoreLevel).isEqualTo(ScoreLevel.VERY_HIGH)
      assertThat(ospiPercentageScore).isEqualTo("5.79")
    }
  }

  @Test
  fun `should return bad request status code for invalid offence code`() {

    val crn = "X234567"
    val requestBody = createOffenderAndOffencesDto(crn, CurrentOffenceDto("138X", "00"))

    webTestClient.post()
      .uri("/risks/predictors/RSR?final=true&source=ASSESSMENTS_API&sourceId=90f2b674-ae1c-488d-8b85-0251708ef6b6")
      .header("Content-Type", "application/json")
      .headers(setAuthorisation(user = "Gary C", roles = listOf("ROLE_PROBATION")))
      .bodyValue(requestBody)
      .exchange()
      .expectStatus().isBadRequest
  }

  @ParameterizedTest
  @MethodSource("currentOffenceDtos")
  fun `should return bad request status code for empty offence codes`(currentOffence: CurrentOffenceDto, expectedErrorMessage: String) {

    val crn = "X234567"
    val requestBody = createOffenderAndOffencesDto(crn, currentOffence)

    val responseBody = webTestClient.post()
      .uri("/risks/predictors/RSR?final=true&source=ASSESSMENTS_API&sourceId=90f2b674-ae1c-488d-8b85-0251708ef6b6")
      .header("Content-Type", "application/json")
      .headers(setAuthorisation(user = "Gary C", roles = listOf("ROLE_PROBATION")))
      .bodyValue(requestBody)
      .exchange()
      .expectStatus().isBadRequest
      .expectBody(ErrorResponse::class.java).returnResult().responseBody

    assertThat(responseBody.developerMessage).contains(expectedErrorMessage)
  }

  private fun createOffenderAndOffencesDto(crn: String, currentOffence: CurrentOffenceDto) = OffenderAndOffencesDto(
    crn = crn,
    gender = Gender.MALE,
    dob = LocalDate.of(2021, 1, 1).minusYears(20),
    assessmentDate = LocalDateTime.of(2021, 1, 1, 0, 0, 0),
    currentOffence = currentOffence,
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
