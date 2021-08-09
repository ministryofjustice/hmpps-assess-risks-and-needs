package uk.gov.justice.digital.hmpps.assessrisksandneeds.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentNeedDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentNeedsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.NeedSeverity
import java.time.LocalDateTime

@AutoConfigureWebTestClient
@DisplayName("Criminogenic Needs Tests")
class AssessmentNeedsControllerTest : IntegrationTestBase() {

  private val crn = "X123456"

  @Test
  fun `get criminogenic needs by crn`() {
    val needsDto = webTestClient.get().uri("/needs/crn/$crn")
      .headers(setAuthorisation(roles = listOf("ROLE_CRS_PROVIDER")))
      .exchange()
      .expectStatus().isOk
      .expectBody<AssessmentNeedsDto>()
      .returnResult().responseBody

    assertThat(needsDto?.assessedOn).isEqualTo(LocalDateTime.of(2021, 6, 21, 15, 55, 4))
    assertThat(needsDto?.identifiedNeeds).containsExactlyInAnyOrderElementsOf(identifiedNeeds())
    assertThat(needsDto?.notIdentifiedNeeds).containsExactlyInAnyOrderElementsOf(scoredNotNeeds())
    assertThat(needsDto?.unansweredNeeds).containsExactlyInAnyOrderElementsOf(unscoredNeeds())
  }

  @Test
  fun `get criminogenic needs returns not found`() {
    webTestClient.get().uri("/needs/crn/NOT_FOUND")
      .headers(setAuthorisation(roles = listOf("ROLE_CRS_PROVIDER")))
      .exchange()
      .expectStatus().isNotFound
  }

  private fun unscoredNeeds() = listOf(
    AssessmentNeedDto(
      section = "THINKING_AND_BEHAVIOUR",
      name = "Thinking and behaviour"
    ),
    AssessmentNeedDto(
      section = "ATTITUDES",
      name = "Attitudes"
    )
  )

  private fun scoredNotNeeds() = listOf(
    AssessmentNeedDto(
      section = "EMOTIONAL_WELL_BEING",
      name = "Emotional Well-Being",
      overThreshold = false,
      riskOfHarm = false,
      riskOfReoffending = false,
      flaggedAsNeed = false,
      severity = NeedSeverity.NO_NEED,
      identifiedAsNeed = false
    ),
    AssessmentNeedDto(
      section = "FINANCIAL_MANAGEMENT_AND_INCOME",
      name = "Financial Management and Income",
      overThreshold = false,
      riskOfHarm = false,
      riskOfReoffending = false,
      flaggedAsNeed = false,
      severity = NeedSeverity.NO_NEED,
      identifiedAsNeed = false
    )
  )

  private fun identifiedNeeds() = listOf(
    AssessmentNeedDto(
      section = "ACCOMMODATION",
      name = "Accommodation",
      overThreshold = false,
      riskOfHarm = true,
      riskOfReoffending = true,
      flaggedAsNeed = false,
      severity = NeedSeverity.SEVERE,
      identifiedAsNeed = true,
      needScore = 2
    ),
    AssessmentNeedDto(
      section = "EDUCATION_TRAINING_AND_EMPLOYABILITY",
      name = "4 - Education, Training and Employability",
      overThreshold = false,
      riskOfHarm = true,
      riskOfReoffending = true,
      flaggedAsNeed = false,
      severity = NeedSeverity.SEVERE,
      identifiedAsNeed = true,
      needScore = 2
    ),
    AssessmentNeedDto(
      section = "RELATIONSHIPS",
      name = "Relationships",
      overThreshold = false,
      riskOfHarm = true,
      riskOfReoffending = true,
      flaggedAsNeed = false,
      severity = NeedSeverity.SEVERE,
      identifiedAsNeed = true,
      needScore = 2
    ),
    AssessmentNeedDto(
      section = "LIFESTYLE_AND_ASSOCIATES",
      name = "Lifestyle and Associates",
      overThreshold = false,
      riskOfHarm = true,
      riskOfReoffending = true,
      flaggedAsNeed = false,
      severity = NeedSeverity.SEVERE,
      identifiedAsNeed = true,
      needScore = 2
    ),
    AssessmentNeedDto(
      section = "DRUG_MISUSE",
      name = "Drug Misuse",
      overThreshold = false,
      riskOfHarm = true,
      riskOfReoffending = true,
      flaggedAsNeed = false,
      severity = NeedSeverity.SEVERE,
      identifiedAsNeed = true,
      needScore = 2
    ),
    AssessmentNeedDto(
      section = "ALCOHOL_MISUSE",
      name = "Alcohol Misuse",
      overThreshold = false,
      riskOfHarm = true,
      riskOfReoffending = true,
      flaggedAsNeed = false,
      severity = NeedSeverity.SEVERE,
      identifiedAsNeed = true,
      needScore = 2
    )
  )
}
