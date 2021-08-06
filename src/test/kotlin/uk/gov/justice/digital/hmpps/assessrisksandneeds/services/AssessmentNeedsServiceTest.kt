package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentNeedDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.NeedSeverity
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OffenderNeedDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.OffenderNeedsDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.AssessmentApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.EntityNotFoundException
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
@DisplayName("Assessment Need Service Tests")
class AssessmentNeedsServiceTest {
  private val assessmentApiRestClient: AssessmentApiRestClient = mockk()
  private val assessmentNeedsService = AssessmentNeedsService(assessmentApiRestClient)

  @Test
  fun `get assessment needs by crn returns identified needs`() {

    val crn = "CRN123"
    val date = LocalDateTime.now()

    every {
      assessmentApiRestClient.getNeedsForCompletedLastYearAssessment(crn)
    } returns OffenderNeedsDto(
      needs = allOffenderNeeds(),
      historicStatus = "CURRENT",
      assessedOn = date
    )

    val needs = assessmentNeedsService.getAssessmentNeeds(crn)
    assertThat(needs.assessedOn).isEqualTo(date)
    assertThat(needs.identifiedNeeds).hasSize(10)
    assertThat(needs.notIdentifiedNeeds).hasSize(0)
    assertThat(needs.unansweredNeeds).hasSize(0)
  }

  @Test
  fun `get assessment needs by crn includes unanswered needs`() {

    val crn = "CRN123"
    val date = LocalDateTime.now()

    every {
      assessmentApiRestClient.getNeedsForCompletedLastYearAssessment(crn)
    } returns OffenderNeedsDto(
      needs = offenderNeedsWithUnanswered(),
      historicStatus = "CURRENT",
      assessedOn = date
    )

    val needs = assessmentNeedsService.getAssessmentNeeds(crn)
    assertThat(needs.assessedOn).isEqualTo(date)
    assertThat(needs.identifiedNeeds).hasSize(8)
    assertThat(needs.notIdentifiedNeeds).hasSize(0)
    assertThat(needs.unansweredNeeds).isEqualTo(unansweredNeeds())
  }

  @Test
  fun `get assessment needs by crn includes not identified needs`() {

    val crn = "CRN123"
    val date = LocalDateTime.now()

    every {
      assessmentApiRestClient.getNeedsForCompletedLastYearAssessment(crn)
    } returns
      OffenderNeedsDto(
        needs = offenderNeedsWithNotIdentified(),
        historicStatus = "CURRENT",
        assessedOn = date
      )

    val needs = assessmentNeedsService.getAssessmentNeeds(crn)
    assertThat(needs.assessedOn).isEqualTo(date)
    assertThat(needs.identifiedNeeds).hasSize(8)
    assertThat(needs.notIdentifiedNeeds).isEqualTo(notIdentifiedNeeds())
    assertThat(needs.unansweredNeeds).hasSize(0)
  }

  @Test
  fun `get assessment needs by crn throws Exception when empty needs found`() {
    val crn = "CRN123"
    val date = LocalDateTime.now()

    every {
      assessmentApiRestClient.getNeedsForCompletedLastYearAssessment(crn)
    } returns OffenderNeedsDto(
      emptyList(),
      date,
      "CURRENT"
    )

    val exception = assertThrows<EntityNotFoundException> {
      assessmentNeedsService.getAssessmentNeeds(crn)
    }
    assertEquals(
      "No needs found for CRN: $crn",
      exception.message
    )
  }

  @Test
  fun `get assessment needs by crn throws Exception when needs are not current`() {
    val crn = "CRN123"
    val date = LocalDateTime.now()

    every {
      assessmentApiRestClient.getNeedsForCompletedLastYearAssessment(crn)
    } returns OffenderNeedsDto(
      allOffenderNeeds(),
      date,
      "HISTORIC"
    )

    val exception = assertThrows<EntityNotFoundException> {
      assessmentNeedsService.getAssessmentNeeds(crn)
    }
    assertEquals(
      "Current needs for CRN: $crn could not be found",
      exception.message
    )
  }

  private fun unansweredNeeds() = listOf(
    AssessmentNeedDto(
      section = "THINKING_AND_BEHAVIOUR",
      name = "Thinking and behaviour"
    ),
    AssessmentNeedDto(
      section = "ATTITUDES",
      name = "Attitudes"
    )
  )

  private fun notIdentifiedNeeds() = listOf(
    AssessmentNeedDto(
      section = "THINKING_AND_BEHAVIOUR",
      name = "Thinking and behaviour",
      overThreshold = false,
      riskOfHarm = false,
      riskOfReoffending = false,
      flaggedAsNeed = false,
      severity = NeedSeverity.NO_NEED,
      identifiedAsNeed = false
    ),
    AssessmentNeedDto(
      section = "ATTITUDES",
      name = "Attitudes",
      overThreshold = false,
      riskOfHarm = false,
      riskOfReoffending = false,
      flaggedAsNeed = false,
      severity = NeedSeverity.NO_NEED,
      identifiedAsNeed = false
    )
  )

  private fun allOffenderNeeds() = listOf(
    OffenderNeedDto(
      section = "EMOTIONAL_WELL_BEING",
      name = "Emotional Well-Being",
      overThreshold = false,
      riskOfHarm = true,
      riskOfReoffending = true,
      flaggedAsNeed = false,
      severity = NeedSeverity.SEVERE,
      identifiedAsNeed = true,
      needScore = 2
    ),
    OffenderNeedDto(
      section = "FINANCIAL_MANAGEMENT_AND_INCOME",
      name = "Financial Management and Income",
      overThreshold = false,
      riskOfHarm = true,
      riskOfReoffending = true,
      flaggedAsNeed = false,
      severity = NeedSeverity.SEVERE,
      identifiedAsNeed = true,
      needScore = 2
    ),
    OffenderNeedDto(
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
    OffenderNeedDto(
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
    OffenderNeedDto(
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
    OffenderNeedDto(
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
    OffenderNeedDto(
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
    OffenderNeedDto(
      section = "ALCOHOL_MISUSE",
      name = "Alcohol Misuse",
      overThreshold = false,
      riskOfHarm = true,
      riskOfReoffending = true,
      flaggedAsNeed = false,
      severity = NeedSeverity.SEVERE,
      identifiedAsNeed = true,
      needScore = 2
    ),
    OffenderNeedDto(
      section = "THINKING_AND_BEHAVIOUR",
      name = "Thinking and behaviour",
      overThreshold = false,
      riskOfHarm = true,
      riskOfReoffending = true,
      flaggedAsNeed = false,
      severity = NeedSeverity.SEVERE,
      identifiedAsNeed = true,
      needScore = 2
    ),
    OffenderNeedDto(
      section = "ATTITUDES",
      name = "Attitudes",
      overThreshold = false,
      riskOfHarm = true,
      riskOfReoffending = true,
      flaggedAsNeed = false,
      severity = NeedSeverity.SEVERE,
      identifiedAsNeed = true,
      needScore = 2
    )
  )

  private fun offenderNeedsWithNotIdentified() = listOf(
    OffenderNeedDto(
      section = "EMOTIONAL_WELL_BEING",
      name = "Emotional Well-Being",
      overThreshold = false,
      riskOfHarm = true,
      riskOfReoffending = true,
      flaggedAsNeed = false,
      severity = NeedSeverity.SEVERE,
      identifiedAsNeed = true,
      needScore = 2
    ),
    OffenderNeedDto(
      section = "FINANCIAL_MANAGEMENT_AND_INCOME",
      name = "Financial Management and Income",
      overThreshold = false,
      riskOfHarm = true,
      riskOfReoffending = true,
      flaggedAsNeed = false,
      severity = NeedSeverity.SEVERE,
      identifiedAsNeed = true,
      needScore = 2
    ),
    OffenderNeedDto(
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
    OffenderNeedDto(
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
    OffenderNeedDto(
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
    OffenderNeedDto(
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
    OffenderNeedDto(
      section = "DRUG_MISUSE",
      name = "Drug Misuse",
      overThreshold = true,
      riskOfHarm = true,
      riskOfReoffending = true,
      flaggedAsNeed = false,
      severity = NeedSeverity.SEVERE,
      identifiedAsNeed = true,
      needScore = 2
    ),
    OffenderNeedDto(
      section = "ALCOHOL_MISUSE",
      name = "Alcohol Misuse",
      overThreshold = true,
      riskOfHarm = true,
      riskOfReoffending = true,
      flaggedAsNeed = false,
      severity = NeedSeverity.SEVERE,
      identifiedAsNeed = true,
      needScore = 2
    ),
    OffenderNeedDto(
      section = "THINKING_AND_BEHAVIOUR",
      name = "Thinking and behaviour",
      overThreshold = false,
      riskOfHarm = false,
      riskOfReoffending = false,
      flaggedAsNeed = false,
      severity = NeedSeverity.NO_NEED,
      identifiedAsNeed = false
    ),
    OffenderNeedDto(
      section = "ATTITUDES",
      name = "Attitudes",
      overThreshold = false,
      riskOfHarm = false,
      riskOfReoffending = false,
      flaggedAsNeed = false,
      severity = NeedSeverity.NO_NEED,
      identifiedAsNeed = false
    )
  )

  private fun offenderNeedsWithUnanswered() = listOf(
    OffenderNeedDto(
      section = "EMOTIONAL_WELL_BEING",
      name = "Emotional Well-Being",
      overThreshold = false,
      riskOfHarm = true,
      riskOfReoffending = true,
      flaggedAsNeed = false,
      severity = NeedSeverity.SEVERE,
      identifiedAsNeed = true,
      needScore = 2
    ),
    OffenderNeedDto(
      section = "FINANCIAL_MANAGEMENT_AND_INCOME",
      name = "Financial Management and Income",
      overThreshold = false,
      riskOfHarm = true,
      riskOfReoffending = true,
      flaggedAsNeed = false,
      severity = NeedSeverity.SEVERE,
      identifiedAsNeed = true,
      needScore = 2
    ),
    OffenderNeedDto(
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
    OffenderNeedDto(
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
    OffenderNeedDto(
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
    OffenderNeedDto(
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
    OffenderNeedDto(
      section = "DRUG_MISUSE",
      name = "Drug Misuse",
      overThreshold = true,
      riskOfHarm = true,
      riskOfReoffending = true,
      flaggedAsNeed = false,
      severity = NeedSeverity.SEVERE,
      identifiedAsNeed = true,
      needScore = 2
    ),
    OffenderNeedDto(
      section = "ALCOHOL_MISUSE",
      name = "Alcohol Misuse",
      overThreshold = true,
      riskOfHarm = true,
      riskOfReoffending = true,
      flaggedAsNeed = false,
      severity = NeedSeverity.SEVERE,
      identifiedAsNeed = true,
      needScore = 2
    )
  )
}
