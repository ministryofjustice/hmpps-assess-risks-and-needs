package uk.gov.justice.digital.hmpps.assessrisksandneeds.integration

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentNeedDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentNeedsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentOffenceDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.NeedSeverity
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.ApiErrorResponse
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.AuditService
import java.time.LocalDateTime

@AutoConfigureWebTestClient(timeout = "360000000")
@DisplayName("Assessment Tests")
class AssessmentControllerTest : IntegrationTestBase() {

  @MockkBean
  private lateinit var auditService: AuditService

  private val crn = "X123456"

  @BeforeEach
  fun setup() {
    every { auditService.sendEvent(any(), any()) } returns Unit
  }

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

    assertThat(assessmentOffenceDto?.crn).isEqualTo(crn)
    assertThat(assessmentOffenceDto?.limitedAccessOffender).isEqualTo(false)
    val assessment1 = assessmentOffenceDto?.assessments?.get(0)
    assertThat(assessment1?.assessmentId).isEqualTo(9630348)
    assertThat(assessment1?.dateCompleted).isEqualTo(LocalDateTime.of(2022, 4, 27, 12, 46, 39))
    assertThat(assessment1?.initiationDate).isEqualTo(LocalDateTime.of(2022, 4, 27, 12, 42, 25))
    assertThat(assessment1?.assessmentStatus).isEqualTo("COMPLETE")
    assertThat(assessment1?.assessmentType).isEqualTo("LAYER1")
    assertThat(assessment1?.partcompStatus).isNull()

    val assessment2 = assessmentOffenceDto?.assessments?.get(1)
    assertThat(assessment2?.assessmentId).isEqualTo(9632348)
    assertThat(assessment2?.dateCompleted).isEqualTo(LocalDateTime.of(2022, 6, 9, 15, 13, 18))
    assertThat(assessment2?.initiationDate).isEqualTo(LocalDateTime.of(2022, 5, 31, 10, 37, 5))
    assertThat(assessment2?.assessmentStatus).isEqualTo("LOCKED_INCOMPLETE")
    assertThat(assessment2?.assessmentType).isEqualTo("LAYER3")
    assertThat(assessment2?.partcompStatus).isEqualTo("Unsigned")

    val assessment3 = assessmentOffenceDto?.assessments?.get(2)
    assertThat(assessment3?.assessmentId).isEqualTo(9634348)
    assertThat(assessment3?.dateCompleted).isEqualTo(LocalDateTime.of(2022, 6, 9, 15, 16, 21))
    assertThat(assessment3?.initiationDate).isEqualTo(LocalDateTime.of(2022, 6, 9, 15, 13, 55))
    assertThat(assessment3?.assessmentStatus).isEqualTo("COMPLETE")
    assertThat(assessment3?.assessmentType).isEqualTo("LAYER3")
    assertThat(assessment3?.partcompStatus).isNull()

    val assessment4 = assessmentOffenceDto?.assessments?.get(3)
    assertThat(assessment4?.assessmentId).isEqualTo(9635350)
    assertThat(assessment4?.dateCompleted).isEqualTo(LocalDateTime.of(2022, 6, 10, 18, 23, 20))
    assertThat(assessment4?.initiationDate).isEqualTo(LocalDateTime.of(2022, 6, 10, 18, 22, 2))
    assertThat(assessment4?.assessmentStatus).isEqualTo("COMPLETE")
    assertThat(assessment4?.assessmentType).isEqualTo("LAYER3")
    assertThat(assessment4?.partcompStatus).isNull()

    val assessment5 = assessmentOffenceDto?.assessments?.get(4)
    assertThat(assessment5?.assessmentId).isEqualTo(9635351)
    assertThat(assessment5?.assessmentType).isEqualTo("LAYER3")
    assertThat(assessment5?.dateCompleted).isEqualTo(LocalDateTime.of(2022, 7, 21, 15, 43, 12))
    assertThat(assessment5?.initiationDate).isEqualTo(LocalDateTime.of(2022, 6, 10, 18, 23, 51))
    assertThat(assessment5?.assessorSignedDate).isEqualTo(LocalDateTime.of(2022, 7, 21, 15, 43, 12))

    assertThat(assessment5?.laterWIPAssessmentExists).isEqualTo(true)
    assertThat(assessment5?.latestWIPDate).isEqualTo(LocalDateTime.of(2022, 7, 21, 15, 43, 58))
    assertThat(assessment5?.laterSignLockAssessmentExists).isEqualTo(false)
    assertThat(assessment5?.latestSignLockDate).isNull()
    assertThat(assessment5?.laterPartCompUnsignedAssessmentExists).isEqualTo(false)
    assertThat(assessment5?.latestPartCompUnsignedDate).isEqualTo(LocalDateTime.of(2022, 5, 31, 10, 37, 5))
    assertThat(assessment5?.laterPartCompSignedAssessmentExists).isEqualTo(false)
    assertThat(assessment5?.latestPartCompSignedDate).isNull()
    assertThat(assessment5?.laterCompleteAssessmentExists).isEqualTo(false)
    assertThat(assessment5?.latestCompleteDate).isEqualTo(LocalDateTime.of(2022, 7, 21, 15, 43, 12))

    assertThat(assessment5?.offence).isEqualTo("TBA")
    assertThat(assessment5?.assessmentStatus).isEqualTo("COMPLETE")
    assertThat(assessment5?.superStatus).isEqualTo("COMPLETE")
    assertThat(assessment5?.disinhibitors?.get(0)).isEqualTo("Alcohol")
    assertThat(assessment5?.patternOfOffending).isEqualTo("TBA")
    assertThat(assessment5?.disinhibitors?.get(0)).isEqualTo("Alcohol")
    assertThat(assessment5?.offenceInvolved?.get(0)).isEqualTo("Carrying or using a weapon")
    assertThat(assessment5?.specificWeapon).isEqualTo("TBA")
    assertThat(assessment5?.victimPerpetratorRelationship).isEqualTo("blah")
    assertThat(assessment5?.victimOtherInfo).isEqualTo("mmmmmm")
    assertThat(assessment5?.evidencedMotivations?.get(0)).isEqualTo("Sexual motivation")

    assertThat(assessment5?.offenceDetails?.get(0)?.type).isEqualTo("CONCURRENT")
    assertThat(assessment5?.offenceDetails?.get(0)?.offenceDate).isEqualTo(
      LocalDateTime.of(2021, 11, 1, 0, 0, 0),
    )
    assertThat(assessment5?.offenceDetails?.get(0)?.offenceCode).isEqualTo("028")
    assertThat(assessment5?.offenceDetails?.get(0)?.offenceSubCode).isEqualTo("00")
    assertThat(assessment5?.offenceDetails?.get(0)?.offence).isEqualTo("Burglary in a dwelling")
    assertThat(assessment5?.offenceDetails?.get(0)?.subOffence).isEqualTo(
      "Burglary in a dwelling    [Use this code only if you are unable to determine which subcoded Offence applies]",
    )

    assertThat(assessment5?.offenceDetails?.get(1)?.type).isEqualTo("CURRENT")
    assertThat(assessment5?.offenceDetails?.get(1)?.offenceDate).isEqualTo(
      LocalDateTime.of(2021, 12, 25, 0, 0, 0),
    )
    assertThat(assessment5?.offenceDetails?.get(1)?.offenceCode).isEqualTo("020")
    assertThat(assessment5?.offenceDetails?.get(1)?.offenceSubCode).isEqualTo("05")
    assertThat(assessment5?.offenceDetails?.get(1)?.offence).isEqualTo("Sexual assault on a female")
    assertThat(assessment5?.offenceDetails?.get(1)?.subOffence).isEqualTo("Sexual assault on a female")

    assertThat(assessment5?.victimDetails?.get(0)?.age).isEqualTo("26-49")
    assertThat(assessment5?.victimDetails?.get(0)?.gender).isEqualTo("Male")
    assertThat(assessment5?.victimDetails?.get(0)?.ethnicCategory).isEqualTo("White - Irish")
    assertThat(assessment5?.victimDetails?.get(0)?.victimRelation).isEqualTo("Stranger")

    assertThat(assessment5?.victimDetails?.get(1)?.age).isEqualTo("50-64")
    assertThat(assessment5?.victimDetails?.get(1)?.gender).isEqualTo("Male")
    assertThat(assessment5?.victimDetails?.get(1)?.ethnicCategory).isEqualTo("Chinese or other ethnic group - Chinese TEST 080212")
    assertThat(assessment5?.victimDetails?.get(1)?.victimRelation).isEqualTo("Spouse/Partner - live in")

    assertThat(assessment5?.partcompStatus).isNull()

    val assessment6 = assessmentOffenceDto?.assessments?.get(5)
    assertThat(assessment6?.assessmentId).isEqualTo(9639348)
    assertThat(assessment6?.dateCompleted).isNull()
    assertThat(assessment6?.initiationDate).isEqualTo(LocalDateTime.of(2022, 7, 21, 15, 43, 58))
    assertThat(assessment6?.assessmentStatus).isEqualTo("OPEN")
    assertThat(assessment6?.assessmentType).isEqualTo("LAYER3")
    assertThat(assessment6?.partcompStatus).isNull()
  }

  @Test
  fun `get assessment offence details with no complete assessments`() {
    val assessmentOffenceDto = webTestClient.get().uri("/assessments/crn/X654321/offence")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isOk
      .expectBody<AssessmentOffenceDto>()
      .returnResult().responseBody

    assertThat(assessmentOffenceDto?.crn).isEqualTo("X654321")
    assertThat(assessmentOffenceDto?.assessments?.size).isEqualTo(2)

    val assessment1 = assessmentOffenceDto?.assessments?.get(0)
    assertThat(assessment1?.dateCompleted).isEqualTo(LocalDateTime.of(2011, 2, 7, 17, 9, 7))
    assertThat(assessment1?.initiationDate).isEqualTo(LocalDateTime.of(2011, 2, 1, 15, 37, 9))
    assertThat(assessment1?.assessmentStatus).isEqualTo("LOCKED_INCOMPLETE")

    val assessment2 = assessmentOffenceDto?.assessments?.get(1)
    assertThat(assessment2?.dateCompleted).isNull()
    assertThat(assessment2?.initiationDate).isEqualTo(LocalDateTime.of(2011, 2, 7, 17, 10, 17))
    assertThat(assessment2?.assessmentStatus).isEqualTo("SIGNED")
  }

  @Test
  fun `get assessment offence details not found`() {
    webTestClient.get().uri("/assessments/crn/NOT_FOUND/offence")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `should return forbidden when user cannot access crn`() {
    webTestClient.get().uri("/assessments/crn/FORBIDDEN/offence")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `should return not found when Delius cannot find crn`() {
    val response = webTestClient.get().uri("/assessments/crn/USER_ACCESS_NOT_FOUND/offence")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isNotFound
      .expectBody<ApiErrorResponse>()
      .returnResult().responseBody

    assertThat(response.developerMessage).isEqualTo("No such offender for CRN: USER_ACCESS_NOT_FOUND")
  }

  private fun unscoredNeeds() = listOf(
    AssessmentNeedDto(
      section = "THINKING_AND_BEHAVIOUR",
      name = "Thinking and behaviour",
    ),
    AssessmentNeedDto(
      section = "ATTITUDES",
      name = "Attitudes",
    ),
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
      identifiedAsNeed = false,
    ),
    AssessmentNeedDto(
      section = "FINANCIAL_MANAGEMENT_AND_INCOME",
      name = "Financial Management and Income",
      overThreshold = false,
      riskOfHarm = false,
      riskOfReoffending = false,
      flaggedAsNeed = false,
      severity = NeedSeverity.NO_NEED,
      identifiedAsNeed = false,
    ),
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
      needScore = 2,
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
      needScore = 2,
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
      needScore = 2,
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
      needScore = 2,
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
      needScore = 2,
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
      needScore = 2,
    ),
  )
}
