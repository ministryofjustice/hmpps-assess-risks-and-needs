package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.aspectj.weaver.tools.cache.SimpleCacheFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.slf4j.MDC
import org.springframework.http.HttpMethod
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentOffenceDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentSummaryIndicator
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentSummaryIndicators
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.BasicAssessmentSummary
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.CaseAccess
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.Indicators
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PersonIdentifier
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.SanIndicatorResponse
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.SexualOffenceDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.Timeline
import uk.gov.justice.digital.hmpps.assessrisksandneeds.config.Clock
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.CommunityApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.ExternalService
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.OasysApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysAssessmentDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysAssessmentOffenceDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.OasysAssessmentWrapper
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.TimelineDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.oasys.section.Assessor
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.oasys.section.OasysSection1
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.EntityNotFoundException
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.ExternalApiForbiddenException
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
class AssessmentOffenceServiceTest {

  private val oasysClient: OasysApiRestClient = mockk()
  private val communityClient: CommunityApiRestClient = mockk()
  private val auditService: AuditService = mockk()
  private val clock: Clock = mockk()
  private val assessmentOffenceService = AssessmentOffenceService(oasysClient, communityClient, auditService, clock)
  private val crn = "T123456"
  private val identifier = PersonIdentifier(PersonIdentifier.Type.CRN, crn)
  private val assessment = BasicAssessmentSummary(6758939181, LocalDateTime.parse("2024-12-25T12:00:00"), LocalDateTime.parse("2024-12-25T12:00:00"), "LAYER3", "COMPLETE")

  @BeforeEach
  fun setup() {
    every { communityClient.verifyUserAccess(any(), any()) } answers {
      CaseAccess(
        it.invocation.args[0] as String,
        userExcluded = false,
        userRestricted = false,
        null,
        null,
      )
    }
    mockkStatic(MDC::class)
    every { MDC.get(any()) } returns "userName"
    every { auditService.sendEvent(any(), any()) } returns Unit
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
          laterWIPAssessmentExists = true,
        ),
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
        ),
      ),
    )
    every { oasysClient.getAssessmentOffence(any(), any()) }.returns(assessmentOffenceDto)

    // When
    val result = assessmentOffenceService.getAssessmentOffence(crn)

    // Then
    verify(exactly = 1) { auditService.sendEvent(EventType.ACCESSED_OFFENCE_DETAILS, mapOf("crn" to crn)) }
    verify(exactly = 1) { oasysClient.getAssessmentOffence(crn, "ALLOW") }
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
              laterWIPAssessmentExists = true,
            ),
          ),
        ),
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
          offenceInvolved = listOf("Carrying or using a weapon"),
        ),
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
        ),
      ),
    )
    every { oasysClient.getAssessmentOffence(any(), any()) }.returns(assessmentOffenceDto)

    // When
    val result = assessmentOffenceService.getAssessmentOffence(crn)

    // Then
    verify(exactly = 1) { oasysClient.getAssessmentOffence(crn, "ALLOW") }
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
            ),
          ),
        ),
      )
  }

  @Test
  fun `should successfully map a offender api response with no complete assessments`() {
    // Given
    val crn = "X12345"
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
        ),
      ),
    )
    every { oasysClient.getAssessmentOffence(any(), any()) }.returns(assessmentOffenceDto)

    // When
    val result = assessmentOffenceService.getAssessmentOffence(crn)

    // Then
    verify(exactly = 1) { oasysClient.getAssessmentOffence(crn, "ALLOW") }
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
          ),
        ),
      )
  }

  @Test
  fun `should throw exception when offence is not found for given CRN`() {
    // Given
    val crn = "DOES_NOT_EXIST"
    every {
      oasysClient.getAssessmentOffence(
        any(),
        any(),
      )
    }.throws(EntityNotFoundException("Bad crn"))

    // When
    assertThrows<EntityNotFoundException> {
      assessmentOffenceService.getAssessmentOffence(crn)
    }
  }

  @Test
  fun `should NOT call get assessment offence when user is forbidden to access CRN`() {
    // Given
    val crn = "X12345"
    every {
      communityClient.verifyUserAccess(
        crn,
        any(),
      )
    }.throws(
      ExternalApiForbiddenException(
        "User does not have permission to access offender with CRN $crn.",
        HttpMethod.GET,
        SimpleCacheFactory.path,
        ExternalService.COMMUNITY_API,
        listOfNotNull("Excluded", "Restricted"),
      ),
    )

    // When
    assertThrows<ExternalApiForbiddenException> { assessmentOffenceService.getAssessmentOffence(crn) }

    // Then
    verify(exactly = 0) { oasysClient.getAssessmentOffence(crn, "ALLOW") }
  }

  @ParameterizedTest
  @CsvSource("empty, false", "N, false", "Y, true")
  fun `returns person cell location if in prison`(sanIndicator: String, result: Boolean) {
    val indicators = when (sanIndicator) {
      "empty" -> Indicators(null)
      else -> Indicators(sanIndicator)
    }
    val assessmentIndicators = AssessmentSummaryIndicators(listOf(AssessmentSummaryIndicator(indicators)))

    every { oasysClient.getLatestAssessment(eq(identifier), any()) } answers { assessment }
    every { oasysClient.getAssessmentSummaryIndicators(eq(assessment), crn) } answers { assessmentIndicators }

    val response = assessmentOffenceService.getSanIndicator(crn)

    assertThat(response).isEqualTo(SanIndicatorResponse(crn, result))

    verify(exactly = 1) { oasysClient.getLatestAssessment(identifier, any()) }
    verify(exactly = 1) { oasysClient.getAssessmentSummaryIndicators(assessment, crn) }
  }

  @Test
  fun `no assessment found for CRN`() {
    every { oasysClient.getLatestAssessment(eq(identifier), any()) } answers { null }

    val response = assertThrows<EntityNotFoundException> { assessmentOffenceService.getSanIndicator(crn) }

    assertThat(response.message).isEqualTo("No assessment found for CRN: $crn")
  }

  @Test
  fun `no san indicator value for CRN`() {
    every { oasysClient.getLatestAssessment(eq(identifier), any()) } answers { assessment }
    every { oasysClient.getAssessmentSummaryIndicators(eq(assessment), crn) } answers {
      AssessmentSummaryIndicators(listOf(AssessmentSummaryIndicator(Indicators(null))))
    }

    val response = assessmentOffenceService.getSanIndicator(crn)
    assertThat(response.sanIndicator).isFalse
  }

  @ParameterizedTest
  @CsvSource("Yes, true", "No, false")
  fun `returns sexually motivated offence details from latest complete layer 3 assessment`(
    everCommittedSexualOffence: String,
    expectedResult: Boolean,
  ) {
    every { oasysClient.getLatestAssessment(eq(identifier), any()) } answers { assessment }
    every { oasysClient.getOffenderInformationAndPredictorsSection(eq(assessment)) } answers {
      OasysAssessmentWrapper(crn, listOf(OasysSection1(everCommittedSexualOffence)))
    }

    val response = assessmentOffenceService.getSexuallyMotivatedOffenceDetails(crn)

    assertThat(response).isEqualTo(SexualOffenceDto(expectedResult))
    verify(exactly = 1) { oasysClient.getLatestAssessment(identifier, any()) }
    verify(exactly = 1) { oasysClient.getOffenderInformationAndPredictorsSection(assessment) }
  }

  @Test
  fun `returns null sexually motivated offence details when latest complete layer 3 assessment has no answer`() {
    every { oasysClient.getLatestAssessment(eq(identifier), any()) } answers { assessment }
    every { oasysClient.getOffenderInformationAndPredictorsSection(eq(assessment)) } answers {
      OasysAssessmentWrapper(crn, listOf(OasysSection1(null)))
    }

    val response = assessmentOffenceService.getSexuallyMotivatedOffenceDetails(crn)

    assertThat(response).isEqualTo(SexualOffenceDto(null))
  }

  @Test
  fun `throws not found when no latest complete layer 3 assessment exists`() {
    every { oasysClient.getLatestAssessment(eq(identifier), any()) } answers { null }

    val response = assertThrows<EntityNotFoundException> {
      assessmentOffenceService.getSexuallyMotivatedOffenceDetails(crn)
    }

    assertThat(response.message).isEqualTo("No assessment found for CRN: $crn")
    verify(exactly = 0) { oasysClient.getOffenderInformationAndPredictorsSection(any()) }
  }

  @Test
  fun `getLatestCompleteAssessmentsForMapps throws when no timeline found`() {
    val identifier = PersonIdentifier(PersonIdentifier.Type.CRN, "X123456")
    every { oasysClient.getAssessmentTimeline(identifier) } returns null

    assertThrows<EntityNotFoundException> {
      assessmentOffenceService.getLatestCompleteAssessmentsForMapps(identifier)
    }.apply {
      assertThat(message).contains("Assessment timeline not found")
    }
  }

  @Test
  fun `Returns only COMPLETE assessments filtered by type`() {
    val identifier = PersonIdentifier(PersonIdentifier.Type.CRN, "X123456")
    val timeline = Timeline(
      listOf(
        BasicAssessmentSummary(1L, LocalDateTime.now().minusMonths(2), LocalDateTime.now().minusMonths(2), "LAYER3", "COMPLETE"),
        BasicAssessmentSummary(2L, LocalDateTime.now().minusMonths(1), LocalDateTime.now().minusMonths(1), "LAYER1", "COMPLETE"),
        BasicAssessmentSummary(3L, LocalDateTime.now(), null, "LAYER3", "OPEN"), // Should be filtered
        BasicAssessmentSummary(4L, LocalDateTime.now(), LocalDateTime.now(), "STANDALONE", "COMPLETE"), // Should be filtered
      ),
    )

    every { oasysClient.getAssessmentTimeline(identifier) } returns timeline
    every { oasysClient.getOffenderInformationAndPredictorsSection(any()) } answers {
      val assessment = firstArg<BasicAssessmentSummary>()
      OasysAssessmentWrapper(
        crn = "X123456",
        assessments = listOf(
          OasysSection1(null, Assessor(""), Assessor("")),
        ),
      )
    }

    val result = assessmentOffenceService.getLatestCompleteAssessmentsForMapps(identifier)

    assertThat(result.assessments).hasSize(2) // Only LAYER1 and LAYER3 COMPLETE
    assertThat(result.assessments).allMatch { it.assessmentStatus == "COMPLETE" }
    assertThat(result.assessments).allMatch { it.assessmentType in listOf("LAYER1", "LAYER3") }
  }

  @Test
  fun `Sorts assessments by completion date descending`() {
    val identifier = PersonIdentifier(PersonIdentifier.Type.CRN, "X123456")
    val date1 = LocalDateTime.of(2024, 1, 1, 12, 0)
    val date2 = LocalDateTime.of(2024, 6, 1, 12, 0)
    val date3 = LocalDateTime.of(2024, 12, 1, 12, 0)

    val timeline = Timeline(
      listOf(
        BasicAssessmentSummary(1L, date1, date1, "LAYER3", "COMPLETE"),
        BasicAssessmentSummary(3L, date3, date3, "LAYER3", "COMPLETE"),
        BasicAssessmentSummary(2L, date2, date2, "LAYER3", "COMPLETE"),
      ),
    )

    every { oasysClient.getAssessmentTimeline(identifier) } returns timeline
    every { oasysClient.getOffenderInformationAndPredictorsSection(any()) } answers {
      OasysAssessmentWrapper(
        crn = "X123456",
        assessments = listOf(
          OasysSection1(null, Assessor("Assessor"), Assessor(null)),
        ),
      )
    }

    val result = assessmentOffenceService.getLatestCompleteAssessmentsForMapps(identifier)

    val dates = result.assessments.map { it.dateCompleted }
    assertThat(dates).containsExactly(date3, date2, date1)
  }

  @Test
  fun `Skips assessments where section1 fetch fails`() {
    val identifier = PersonIdentifier(PersonIdentifier.Type.CRN, "X123456")
    val timeline = Timeline(
      listOf(
        BasicAssessmentSummary(1L, LocalDateTime.now(), LocalDateTime.now(), "LAYER3", "COMPLETE"),
        BasicAssessmentSummary(2L, LocalDateTime.now(), LocalDateTime.now(), "LAYER3", "COMPLETE"),
      ),
    )

    every { oasysClient.getAssessmentTimeline(identifier) } returns timeline
    // First call fails, second succeeds
    every { oasysClient.getOffenderInformationAndPredictorsSection(any()) } answers {
      val assessment = firstArg<BasicAssessmentSummary>()
      if (assessment.assessmentId == 1L) {
        throw RuntimeException("Section1 fetch failed")
      } else {
        OasysAssessmentWrapper(
          crn = "X123456",
          assessments = listOf(OasysSection1(null, Assessor("Assessor"), Assessor(null))),
        )
      }
    }

    val result = assessmentOffenceService.getLatestCompleteAssessmentsForMapps(identifier)

    // Should have only 1 assessment (the successful one)
    assertThat(result.assessments).hasSize(1)
    assertThat(result.assessments[0].assessmentId).isEqualTo(2L)
  }

  @Test
  fun `Throws when all section1 fetches fail`() {
    val identifier = PersonIdentifier(PersonIdentifier.Type.CRN, "X123456")
    val timeline = Timeline(
      listOf(
        BasicAssessmentSummary(1L, LocalDateTime.now(), LocalDateTime.now(), "LAYER3", "COMPLETE"),
      ),
    )

    every { oasysClient.getAssessmentTimeline(identifier) } returns timeline
    every { oasysClient.getOffenderInformationAndPredictorsSection(any()) } throws RuntimeException("All failed")

    assertThrows<EntityNotFoundException> {
      assessmentOffenceService.getLatestCompleteAssessmentsForMapps(identifier)
    }.apply {
      assertThat(message).contains("No assessments with valid section1 data found")
    }
  }

  @Test
  fun `Handles null countersigner gracefully`() {
    val identifier = PersonIdentifier(PersonIdentifier.Type.CRN, "X123456")
    val timeline = Timeline(
      listOf(
        BasicAssessmentSummary(1L, LocalDateTime.now(), LocalDateTime.now(), "LAYER3", "COMPLETE"),
      ),
    )

    every { oasysClient.getAssessmentTimeline(identifier) } returns timeline
    every { oasysClient.getOffenderInformationAndPredictorsSection(any()) } returns OasysAssessmentWrapper(
      crn = "X123456",
      assessments = listOf(
        OasysSection1(null, Assessor("Assessor Name"), Assessor(null)), // No countersigner
      ),
    )

    val result = assessmentOffenceService.getLatestCompleteAssessmentsForMapps(identifier)

    assertThat(result.assessments[0].assessorName).isEqualTo("Assessor Name")
    assertThat(result.assessments[0].countersignerName).isNull()
  }
}
