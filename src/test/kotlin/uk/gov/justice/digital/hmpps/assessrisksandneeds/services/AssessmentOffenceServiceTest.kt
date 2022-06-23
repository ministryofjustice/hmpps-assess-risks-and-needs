package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentOffenceDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.TimelineDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.AssessmentApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.EntityNotFoundException
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
class AssessmentOffenceServiceTest {

  @MockK
  private val assessmentClient: AssessmentApiRestClient = mockk()
  private val assessmentOffenceService = AssessmentOffenceService(assessmentClient)

  @Test
  fun `should call offender Api to retrieve offence data for a given CRN`() {
    // Given
    val crn = "X12345"
    val dateCompleted = LocalDateTime.of(2022, 1, 7, 12, 0)
    val initiationDate = LocalDateTime.of(2022, 1, 3, 12, 0)
    val assessmentOffenceDto = AssessmentOffenceDto(
      crn = crn,
      assessments = listOf(
        AssessmentDto(
          assessmentId = 3,
          dateCompleted = dateCompleted,
          initiationDate = initiationDate,
          assessmentStatus = "COMPLETE",
          patternOfOffending = "patternOfOffending",
          offenceInvolved = listOf("Carrying or using a weapon")
        )
      ),
      timeline = listOf(
        TimelineDto(
          assessmentId = 1,
          initiationDate = LocalDateTime.of(2022, 1, 1, 12, 0),
          status = "LOCKED_INCOMPLETE",
          completedDate = LocalDateTime.of(2022, 1, 5, 12, 0),
        ),
        TimelineDto(
          assessmentId = 2,
          initiationDate = LocalDateTime.of(2022, 1, 2, 12, 0),
          status = "SIGNED",
          completedDate = null,
        ),
        TimelineDto(
          assessmentId = 3,
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
    verify(exactly = 1) { assessmentClient.getAssessmentOffence(crn, "LIMIT") }
    assertThat(result.timeline).isEmpty()
    assertThat(result)
      .isEqualTo(
        AssessmentOffenceDto(
          crn = crn,
          assessments = listOf(
            AssessmentDto(
              assessmentId = 1,
              dateCompleted = LocalDateTime.of(2022, 1, 5, 12, 0),
              initiationDate = LocalDateTime.of(2022, 1, 1, 12, 0),
              assessmentStatus = "LOCKED_INCOMPLETE",
            ),
            AssessmentDto(
              assessmentId = 2,
              dateCompleted = null,
              initiationDate = LocalDateTime.of(2022, 1, 2, 12, 0),
              assessmentStatus = "SIGNED",
            ),
            AssessmentDto(
              assessmentId = 3,
              dateCompleted = dateCompleted,
              initiationDate = initiationDate,
              assessmentStatus = "COMPLETE",
              patternOfOffending = "patternOfOffending",
              offenceInvolved = listOf("Carrying or using a weapon")
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
    val assessmentOffenceDto = AssessmentOffenceDto(
      crn = crn,
      assessments = listOf(
        AssessmentDto(
          assessmentId = 3,
          dateCompleted = dateCompleted,
          initiationDate = initiationDate,
          assessmentStatus = "COMPLETE",
          patternOfOffending = "patternOfOffending",
          offenceInvolved = listOf("Carrying or using a weapon")
        )
      ),
      timeline = listOf(
        TimelineDto(
          assessmentId = 1,
          initiationDate = LocalDateTime.of(2022, 1, 1, 12, 0),
          status = "LOCKED_INCOMPLETE",
          completedDate = LocalDateTime.of(2022, 1, 5, 12, 0),
        ),
        TimelineDto(
          assessmentId = 2,
          initiationDate = LocalDateTime.of(2022, 1, 2, 12, 0),
          status = "SIGNED",
          completedDate = null,
        ),
        TimelineDto(
          assessmentId = 3,
          initiationDate = initiationDate,
          status = "COMPLETE",
          completedDate = dateCompleted,
        ),
        TimelineDto(
          assessmentId = 4,
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
    verify(exactly = 1) { assessmentClient.getAssessmentOffence(crn, "LIMIT") }
    assertThat(result.timeline).isEmpty()
    assertThat(result)
      .isEqualTo(
        AssessmentOffenceDto(
          crn = crn,
          assessments = listOf(
            AssessmentDto(
              assessmentId = 1,
              dateCompleted = LocalDateTime.of(2022, 1, 5, 12, 0),
              initiationDate = LocalDateTime.of(2022, 1, 1, 12, 0),
              assessmentStatus = "LOCKED_INCOMPLETE",
            ),
            AssessmentDto(
              assessmentId = 2,
              dateCompleted = null,
              initiationDate = LocalDateTime.of(2022, 1, 2, 12, 0),
              assessmentStatus = "SIGNED",
            ),
            AssessmentDto(
              assessmentId = 3,
              dateCompleted = dateCompleted,
              initiationDate = initiationDate,
              assessmentStatus = "COMPLETE",
              patternOfOffending = "patternOfOffending",
              offenceInvolved = listOf("Carrying or using a weapon")
            ),
            AssessmentDto(
              assessmentId = 4,
              dateCompleted = dateCompleted.plusDays(1),
              initiationDate = initiationDate.plusDays(1),
              assessmentStatus = "COMPLETE",
              patternOfOffending = null,
              offenceInvolved = emptyList()
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
    val assessmentOffenceDto = AssessmentOffenceDto(
      crn = crn,
      timeline = listOf(
        TimelineDto(
          assessmentId = 1,
          initiationDate = LocalDateTime.of(2022, 1, 1, 12, 0),
          status = "LOCKED_INCOMPLETE",
          completedDate = LocalDateTime.of(2022, 1, 5, 12, 0),
        ),
        TimelineDto(
          assessmentId = 2,
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
    verify(exactly = 1) { assessmentClient.getAssessmentOffence(crn, "LIMIT") }
    assertThat(result.timeline).isEmpty()
    assertThat(result)
      .isEqualTo(
        AssessmentOffenceDto(
          crn = crn,
          assessments = listOf(
            AssessmentDto(
              assessmentId = 1,
              dateCompleted = LocalDateTime.of(2022, 1, 5, 12, 0),
              initiationDate = LocalDateTime.of(2022, 1, 1, 12, 0),
              assessmentStatus = "LOCKED_INCOMPLETE",
            ),
            AssessmentDto(
              assessmentId = 2,
              dateCompleted = null,
              initiationDate = LocalDateTime.of(2022, 1, 2, 12, 0),
              assessmentStatus = "SIGNED",
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
}
