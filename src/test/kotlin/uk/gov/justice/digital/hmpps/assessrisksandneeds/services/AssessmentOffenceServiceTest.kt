package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentOffenceDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.AssessmentApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.EntityNotFoundException

@ExtendWith(MockKExtension::class)
class AssessmentOffenceServiceTest {

  @MockK
  private val assessmentClient: AssessmentApiRestClient = mockk()
  private val assessmentOffenceService = AssessmentOffenceService(assessmentClient)

  @Test
  fun `should call offender Api to retrieve offence data for a given CRN`() {
    // Given
    val crn = "X12345"
    every { assessmentClient.getAssessmentOffence(any(), any(), any(), any()) }.returns(AssessmentOffenceDto(crn = crn))

    // When
    assessmentOffenceService.getAssessmentOffence(crn)

    // Then
    verify(exactly = 1) { assessmentClient.getAssessmentOffence(crn, "LIMIT", "COMPLETE", 0) }
  }

  @Test
  fun `should throw exception when offence is not found for given CRN`() {
    // Given
    val crn = "DOES_NOT_EXIST"
    every {
      assessmentClient.getAssessmentOffence(
        any(),
        any(),
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
