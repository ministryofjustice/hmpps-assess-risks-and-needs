package uk.gov.justice.digital.hmpps.assessrisksandneeds.integration

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentNeedDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentNeedsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentOffenceDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.NeedSeverity
import java.time.LocalDateTime

@AutoConfigureWebTestClient
@DisplayName("Assessment Tests")
class AssessmentControllerTest : IntegrationTestBase() {

  private val crn = "X123456"

  @Test
  fun `get criminogenic needs by crn`() {
    val needsDto = webTestClient.get().uri("/needs/crn/$crn")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
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
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `get assessment offence details by crn`() {
    val assessmentOffenceDto = webTestClient.get().uri("/assessments/crn/$crn/offence")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isOk
      .expectBody<AssessmentOffenceDto>()
      .returnResult().responseBody

    println(jacksonObjectMapper().registerModule(JavaTimeModule()).configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false).writeValueAsString(assessmentOffenceDto))

    assertThat(assessmentOffenceDto?.crn).isEqualTo(crn)
    val assessment = assessmentOffenceDto?.assessments?.get(0)
    assertThat(assessment?.dateCompleted).isEqualTo(LocalDateTime.of(2022, 6, 15, 18, 23, 52))
    assertThat(assessment?.offence).isEqualTo("TBA")
    assertThat(assessment?.assessmentStatus).isEqualTo("COMPLETE")
    assertThat(assessment?.disinhibitors?.get(0)).isEqualTo("Alcohol")
    assertThat(assessment?.patternOfOffending).isEqualTo("TBA")
    assertThat(assessment?.disinhibitors?.get(0)).isEqualTo("Alcohol")
    assertThat(assessment?.offenceInvolved?.get(0)).isEqualTo("Carrying or using a weapon")
    assertThat(assessment?.specificWeapon).isEqualTo("TBA")
    assertThat(assessment?.victimPerpetratorRelationship).isEqualTo("mmmmmm")
    assertThat(assessment?.victimOtherInfo).isEqualTo("blah")
    assertThat(assessment?.evidencedMotivations?.get(0)).isEqualTo("Sexual motivation")

    assertThat(assessment?.offenceDetails?.get(0)?.type).isEqualTo("CONCURRENT")
    assertThat(assessment?.offenceDetails?.get(0)?.offenceDate).isEqualTo(
      LocalDateTime.of(2021, 11, 1, 0, 0, 0)
    )
    assertThat(assessment?.offenceDetails?.get(0)?.offenceCode).isEqualTo("028")
    assertThat(assessment?.offenceDetails?.get(0)?.offenceSubCode).isEqualTo("00")
    assertThat(assessment?.offenceDetails?.get(0)?.offence).isEqualTo("Burglary in a dwelling")
    assertThat(assessment?.offenceDetails?.get(0)?.subOffence).isEqualTo(
      "Burglary in a dwelling    [Use this code only if you are unable to determine which subcoded Offence applies]"
    )

    assertThat(assessment?.offenceDetails?.get(1)?.type).isEqualTo("CURRENT")
    assertThat(assessment?.offenceDetails?.get(1)?.offenceDate).isEqualTo(
      LocalDateTime.of(2021, 12, 25, 0, 0, 0)
    )
    assertThat(assessment?.offenceDetails?.get(1)?.offenceCode).isEqualTo("020")
    assertThat(assessment?.offenceDetails?.get(1)?.offenceSubCode).isEqualTo("05")
    assertThat(assessment?.offenceDetails?.get(1)?.offence).isEqualTo("Sexual assault on a female")
    assertThat(assessment?.offenceDetails?.get(1)?.subOffence).isEqualTo("Sexual assault on a female")

    assertThat(assessment?.victimDetails?.get(0)?.age).isEqualTo("21-25")
    assertThat(assessment?.victimDetails?.get(0)?.gender).isEqualTo("Male")
    assertThat(assessment?.victimDetails?.get(0)?.ethnicCategory).isEqualTo("White - Irish")
    assertThat(assessment?.victimDetails?.get(0)?.victimRelation).isEqualTo("Stranger")

    val timeLine = assessmentOffenceDto?.timeLine
    assertThat(timeLine).hasSize(4)

    assertThat(timeLine?.get(0)?.initiationDate).isEqualTo(LocalDateTime.of(2011, 2, 1, 15, 37, 9))
    assertThat(timeLine?.get(0)?.status).isEqualTo("LOCKED_INCOMPLETE")
    assertThat(timeLine?.get(0)?.completedDate).isEqualTo(LocalDateTime.of(2011, 2, 7, 17, 9, 7))

    assertThat(timeLine?.get(1)?.initiationDate).isEqualTo(LocalDateTime.of(2011, 2, 7, 17, 10, 17))
    assertThat(timeLine?.get(1)?.status).isEqualTo("COMPLETE")
    assertThat(timeLine?.get(1)?.completedDate).isEqualTo(LocalDateTime.of(2011, 2, 8, 17, 57, 50))

    assertThat(timeLine?.get(2)?.initiationDate).isEqualTo(LocalDateTime.of(2011, 2, 28, 16, 21, 0))
    assertThat(timeLine?.get(2)?.status).isEqualTo("COMPLETE")
    assertThat(timeLine?.get(2)?.completedDate).isEqualTo(LocalDateTime.of(2011, 2, 28, 19, 3, 44))

    assertThat(timeLine?.get(3)?.initiationDate).isEqualTo(LocalDateTime.of(2011, 2, 28, 19, 5, 38))
    assertThat(timeLine?.get(3)?.status).isEqualTo("COMPLETE")
    assertThat(timeLine?.get(3)?.completedDate).isEqualTo(LocalDateTime.of(2011, 2, 28, 19, 27, 7))
  }

  @Test
  fun `get assessment offence details not found`() {
    webTestClient.get().uri("/assessments/crn/NOT_FOUND/offence")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
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
