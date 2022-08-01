package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.MDC
import org.springframework.http.HttpMethod
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentOffenceDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.UserAccessResponse
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.AssessmentApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.CommunityApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.ExternalService
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysAssessmentDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysAssessmentOffenceDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.TimelineDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.EntityNotFoundException
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.ExternalApiEntityNotFoundException
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
class AssessmentOffenceServiceTest {

  private val assessmentClient: AssessmentApiRestClient = mockk()
  private val communityClient: CommunityApiRestClient = mockk()
  private val assessmentOffenceService = AssessmentOffenceService(assessmentClient, communityClient)

  @BeforeEach
  fun setup() {
    every { communityClient.verifyUserAccess(any(), any()) } returns UserAccessResponse(null, null, false, false)
    mockkStatic(MDC::class)
    every { MDC.get(any()) } returns "userName"
  }

  @Test
  fun `should call offender Api to retrieve offence data for a given CRN`() {
    // Given
    val crn = "X12345"
    val dateCompleted = LocalDateTime.of(2022, 1, 7, 12, 0)
    val initiationDate = LocalDateTime.of(2022, 1, 3, 12, 0)
    val assessmentOffenceDto = OasysAssessmentOffenceDto(
      crn = crn,
      limitedAccessOffender = false,
      assessments = listOf(
        OasysAssessmentDto(
          assessmentPk = 3,
          assessmentType = "LAYER1",
          dateCompleted = dateCompleted,
          initiationDate = initiationDate,
          assessmentStatus = "COMPLETE",
          patternOfOffending = "patternOfOffending",
          offenceInvolved = listOf("Carrying or using a weapon"),
          laterWIPAssessmentExists = true
        )
      ),
      timeline = listOf(
        TimelineDto(
          assessmentPk = 1,
          assessmentType = "LAYER1",
          initiationDate = LocalDateTime.of(2022, 1, 1, 12, 0),
          status = "LOCKED_INCOMPLETE",
          completedDate = LocalDateTime.of(2022, 1, 5, 12, 0),
          partcompStatus = "Signed",
        ),
        TimelineDto(
          assessmentPk = 2,
          assessmentType = "LAYER1",
          initiationDate = LocalDateTime.of(2022, 1, 2, 12, 0),
          status = "SIGNED",
          completedDate = null,
        ),
        TimelineDto(
          assessmentPk = 3,
          assessmentType = "LAYER1",
          initiationDate = initiationDate,
          status = "COMPLETE",
          completedDate = dateCompleted,
        )
      )
    )
    every { assessmentClient.getAssessmentOffence(any(), any()) }.returns(assessmentOffenceDto)

    // When
    val result = assessmentOffenceService.getAssessmentOffence(crn)

    // Then
    verify(exactly = 1) { assessmentClient.getAssessmentOffence(crn, "ALLOW") }
    assertThat(result)
      .isEqualTo(
        AssessmentOffenceDto(
          crn = crn,
          limitedAccessOffender = false,
          assessments = listOf(
            AssessmentDto(
              assessmentId = 1,
              dateCompleted = LocalDateTime.of(2022, 1, 5, 12, 0),
              initiationDate = LocalDateTime.of(2022, 1, 1, 12, 0),
              assessmentStatus = "LOCKED_INCOMPLETE",
              assessmentType = "LAYER1",
              partcompStatus = "Signed",
            ),
            AssessmentDto(
              assessmentId = 2,
              dateCompleted = null,
              initiationDate = LocalDateTime.of(2022, 1, 2, 12, 0),
              assessmentStatus = "SIGNED",
              assessmentType = "LAYER1",
            ),
            AssessmentDto(
              assessmentId = 3,
              dateCompleted = dateCompleted,
              initiationDate = initiationDate,
              assessmentStatus = "COMPLETE",
              patternOfOffending = "patternOfOffending",
              offenceInvolved = listOf("Carrying or using a weapon"),
              assessmentType = "LAYER1",
              laterWIPAssessmentExists = true
            )
          )
        )
      )
  }

  @Test
  fun `should successfully map a offender api response where not all complete assessments are present`() {
    // Given
    val crn = "X12345"
    val dateCompleted = LocalDateTime.of(2022, 1, 7, 12, 0)
    val initiationDate = LocalDateTime.of(2022, 1, 3, 12, 0)
    val assessmentOffenceDto = OasysAssessmentOffenceDto(
      crn = crn,
      limitedAccessOffender = false,
      assessments = listOf(
        OasysAssessmentDto(
          assessmentPk = 3,
          assessmentType = "LAYER1",
          dateCompleted = dateCompleted,
          initiationDate = initiationDate,
          assessmentStatus = "COMPLETE",
          patternOfOffending = "patternOfOffending",
          offenceInvolved = listOf("Carrying or using a weapon")
        )
      ),
      timeline = listOf(
        TimelineDto(
          assessmentPk = 1,
          assessmentType = "LAYER1",
          initiationDate = LocalDateTime.of(2022, 1, 1, 12, 0),
          status = "LOCKED_INCOMPLETE",
          completedDate = LocalDateTime.of(2022, 1, 5, 12, 0),
        ),
        TimelineDto(
          assessmentPk = 2,
          assessmentType = "LAYER1",
          initiationDate = LocalDateTime.of(2022, 1, 2, 12, 0),
          status = "SIGNED",
          completedDate = null,
        ),
        TimelineDto(
          assessmentPk = 3,
          assessmentType = "LAYER1",
          initiationDate = initiationDate,
          status = "COMPLETE",
          completedDate = dateCompleted,
        ),
        TimelineDto(
          assessmentPk = 4,
          assessmentType = "LAYER1",
          initiationDate = initiationDate.plusDays(1),
          status = "COMPLETE",
          completedDate = dateCompleted.plusDays(1),
        )
      )
    )
    every { assessmentClient.getAssessmentOffence(any(), any()) }.returns(assessmentOffenceDto)

    // When
    val result = assessmentOffenceService.getAssessmentOffence(crn)

    // Then
    verify(exactly = 1) { assessmentClient.getAssessmentOffence(crn, "ALLOW") }
    assertThat(result)
      .isEqualTo(
        AssessmentOffenceDto(
          crn = crn,
          limitedAccessOffender = false,
          assessments = listOf(
            AssessmentDto(
              assessmentId = 1,
              dateCompleted = LocalDateTime.of(2022, 1, 5, 12, 0),
              initiationDate = LocalDateTime.of(2022, 1, 1, 12, 0),
              assessmentStatus = "LOCKED_INCOMPLETE",
              assessmentType = "LAYER1",
            ),
            AssessmentDto(
              assessmentId = 2,
              dateCompleted = null,
              initiationDate = LocalDateTime.of(2022, 1, 2, 12, 0),
              assessmentStatus = "SIGNED",
              assessmentType = "LAYER1",
            ),
            AssessmentDto(
              assessmentId = 3,
              dateCompleted = dateCompleted,
              initiationDate = initiationDate,
              assessmentStatus = "COMPLETE",
              patternOfOffending = "patternOfOffending",
              offenceInvolved = listOf("Carrying or using a weapon"),
              assessmentType = "LAYER1",
            ),
            AssessmentDto(
              assessmentId = 4,
              dateCompleted = dateCompleted.plusDays(1),
              initiationDate = initiationDate.plusDays(1),
              assessmentStatus = "COMPLETE",
              patternOfOffending = null,
              offenceInvolved = emptyList(),
              assessmentType = "LAYER1",
            )
          )
        )
      )
  }

  @Test
  fun `should successfully map a offender api response with no complete assessments`() {
    // Given
    val crn = "X12345"
    val dateCompleted = LocalDateTime.of(2022, 1, 7, 12, 0)
    val initiationDate = LocalDateTime.of(2022, 1, 3, 12, 0)
    val assessmentOffenceDto = OasysAssessmentOffenceDto(
      crn = crn,
      limitedAccessOffender = false,
      timeline = listOf(
        TimelineDto(
          assessmentPk = 1,
          assessmentType = "LAYER1",
          initiationDate = LocalDateTime.of(2022, 1, 1, 12, 0),
          status = "LOCKED_INCOMPLETE",
          completedDate = LocalDateTime.of(2022, 1, 5, 12, 0),
        ),
        TimelineDto(
          assessmentPk = 2,
          assessmentType = "LAYER1",
          initiationDate = LocalDateTime.of(2022, 1, 2, 12, 0),
          status = "SIGNED",
          completedDate = null,
        )
      )
    )
    every { assessmentClient.getAssessmentOffence(any(), any()) }.returns(assessmentOffenceDto)

    // When
    val result = assessmentOffenceService.getAssessmentOffence(crn)

    // Then
    verify(exactly = 1) { assessmentClient.getAssessmentOffence(crn, "ALLOW") }
    assertThat(result)
      .isEqualTo(
        AssessmentOffenceDto(
          crn = crn,
          limitedAccessOffender = false,
          assessments = listOf(
            AssessmentDto(
              assessmentId = 1,
              dateCompleted = LocalDateTime.of(2022, 1, 5, 12, 0),
              initiationDate = LocalDateTime.of(2022, 1, 1, 12, 0),
              assessmentStatus = "LOCKED_INCOMPLETE",
              assessmentType = "LAYER1",
            ),
            AssessmentDto(
              assessmentId = 2,
              dateCompleted = null,
              initiationDate = LocalDateTime.of(2022, 1, 2, 12, 0),
              assessmentStatus = "SIGNED",
              assessmentType = "LAYER1",
            )
          )
        )
      )
  }

  @Test
  fun `should throw exception when offence is not found for given CRN`() {
    // Given
    val crn = "DOES_NOT_EXIST"
    every {
      assessmentClient.getAssessmentOffence(
        any(),
        any()
      )
    }.throws(EntityNotFoundException("Bad crn"))

    // When
    assertThrows<EntityNotFoundException>() {
      assessmentOffenceService.getAssessmentOffence(crn)
    }
  }

  @Test
  fun `should NOT call get assessment offence when user is forbidden to access CRN`() {
    // Given
    val crn = "X12345"
    every { communityClient.verifyUserAccess(crn, any()) }.throws(ExternalApiEntityNotFoundException(msg = "User cannot access $crn", HttpMethod.GET, "url", ExternalService.COMMUNITY_API))

    // When
    assertThrows<ExternalApiEntityNotFoundException> { assessmentOffenceService.getAssessmentOffence(crn) }

    // Then
    verify(exactly = 0) { assessmentClient.getAssessmentOffence(crn, "ALLOW") }
  }
}
