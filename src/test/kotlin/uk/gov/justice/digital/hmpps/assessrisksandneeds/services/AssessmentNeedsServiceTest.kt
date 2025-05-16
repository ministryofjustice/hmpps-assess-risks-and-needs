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
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.BasicAssessmentSummary
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.NeedSeverity
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PersonIdentifier
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.OasysApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.SectionSummary
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.oasys.section.OasysThreshold
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.oasys.section.ScoredSection
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.oasys.section.TierThreshold
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.EntityNotFoundException
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
@DisplayName("Assessment Need Service Tests")
class AssessmentNeedsServiceTest {
  private val oasysApiRestClient: OasysApiRestClient = mockk()
  private val assessmentNeedsService = AssessmentNeedsService(oasysApiRestClient)

  @Test
  fun `get assessment needs by crn returns identified needs`() {
    val identifier = PersonIdentifier(PersonIdentifier.Type.CRN, "T123456")
    val assessment = BasicAssessmentSummary(6758939181, LocalDateTime.now(), "LAYER3", "COMPLETE")

    every { oasysApiRestClient.getLatestAssessment(eq(identifier), any()) } answers { assessment }
    every {
      oasysApiRestClient.getScoredSectionsForAssessment(assessment, NeedsSection.entries)
    } answers { allOffenderNeeds(assessment) }

    val needs = assessmentNeedsService.getAssessmentNeeds(identifier.value)
    assertThat(needs.assessedOn).isEqualTo(assessment.completedDate)
    assertThat(needs.identifiedNeeds).hasSize(8)
    assertThat(needs.notIdentifiedNeeds).hasSize(0)
    assertThat(needs.unansweredNeeds).hasSize(0)
  }

  @Test
  fun `get assessment needs by crn includes unanswered needs`() {
    val identifier = PersonIdentifier(PersonIdentifier.Type.CRN, "T123456")
    val assessment = BasicAssessmentSummary(289457671, LocalDateTime.now(), "LAYER3", "COMPLETE")

    every { oasysApiRestClient.getLatestAssessment(eq(identifier), any()) } answers { assessment }
    every {
      oasysApiRestClient.getScoredSectionsForAssessment(assessment, NeedsSection.entries)
    } answers { offenderNeedsWithUnanswered(assessment) }

    val needs = assessmentNeedsService.getAssessmentNeeds(identifier.value)
    assertThat(needs.assessedOn).isEqualTo(assessment.completedDate)
    assertThat(needs.identifiedNeeds).hasSize(6)
    assertThat(needs.notIdentifiedNeeds).hasSize(0)
    assertThat(needs.unansweredNeeds).isEqualTo(unansweredNeeds())
  }

  @Test
  fun `get assessment needs by crn includes not identified needs`() {
    val identifier = PersonIdentifier(PersonIdentifier.Type.CRN, "T123456")
    val assessment = BasicAssessmentSummary(6758939181, LocalDateTime.now(), "LAYER3", "COMPLETE")

    every { oasysApiRestClient.getLatestAssessment(eq(identifier), any()) } answers { assessment }
    every {
      oasysApiRestClient.getScoredSectionsForAssessment(assessment, NeedsSection.entries)
    } answers { offenderNeedsWithNotIdentified(assessment) }

    val needs = assessmentNeedsService.getAssessmentNeeds(identifier.value)
    assertThat(needs.assessedOn).isEqualTo(assessment.completedDate)
    assertThat(needs.identifiedNeeds).hasSize(6)
    assertThat(needs.notIdentifiedNeeds).isEqualTo(notIdentifiedNeeds())
    assertThat(needs.unansweredNeeds).hasSize(0)
  }

  @Test
  fun `get assessment needs by crn throws Exception when empty needs found`() {
    val identifier = PersonIdentifier(PersonIdentifier.Type.CRN, "N123456")
    every { oasysApiRestClient.getLatestAssessment(eq(identifier), any()) } answers { null }

    val exception = assertThrows<EntityNotFoundException> {
      assessmentNeedsService.getAssessmentNeeds(identifier.value)
    }
    assertEquals(
      "No needs found for CRN: ${identifier.value}",
      exception.message,
    )
  }

  private fun unansweredNeeds() = listOf(
    AssessmentNeedDto(
      section = NeedsSection.THINKING_AND_BEHAVIOUR.name,
      name = NeedsSection.THINKING_AND_BEHAVIOUR.description,
      score = null,
      oasysThreshold = OasysThreshold(standard = 4),
      tierThreshold = TierThreshold(standard = 4, severe = 7),
    ),
    AssessmentNeedDto(
      section = NeedsSection.ATTITUDE.name,
      name = NeedsSection.ATTITUDE.description,
      score = null,
      oasysThreshold = OasysThreshold(standard = 2),
      tierThreshold = TierThreshold(standard = 2, severe = 7),
    ),
  )

  private fun notIdentifiedNeeds() = listOf(
    AssessmentNeedDto(
      section = NeedsSection.THINKING_AND_BEHAVIOUR.name,
      name = NeedsSection.THINKING_AND_BEHAVIOUR.description,
      riskOfReoffending = false,
      riskOfHarm = false,
      severity = NeedSeverity.NO_NEED,
      score = 0,
      oasysThreshold = OasysThreshold(standard = 4),
      tierThreshold = TierThreshold(standard = 4, severe = 7),
    ),
    AssessmentNeedDto(
      section = NeedsSection.ATTITUDE.name,
      name = NeedsSection.ATTITUDE.description,
      riskOfReoffending = false,
      riskOfHarm = false,
      severity = NeedSeverity.NO_NEED,
      score = 0,
      oasysThreshold = OasysThreshold(standard = 2),
      tierThreshold = TierThreshold(standard = 2, severe = 7),
    ),
  )

  private fun allOffenderNeeds(assessmentSummary: BasicAssessmentSummary) = SectionSummary(
    assessmentSummary,
    ScoredSection.Accommodation(
      accLinkedToHarm = "Yes",
      accLinkedToReoffending = "Yes",
      noFixedAbodeOrTransient = "NO",
      suitabilityOfAccommodation = "1-Some problems",
      locationOfAccommodation = "2-Significant problems",
      permanenceOfAccommodation = "1-Some problems",
    ),
    ScoredSection.EducationTrainingEmployability(
      eTeLinkedToHarm = "Yes",
      eTeLinkedToReoffending = "Yes",
      unemployed = "0-No",
      workRelatedSkills = "0-No problems",
      employmentHistory = "2-Significant problems",
      attitudeToEmployment = "1-Some problems",
    ),
    ScoredSection.Relationships(
      relLinkedToHarm = "No",
      relLinkedToReoffending = "Yes",
      relParentalResponsibilities = "No",
      experienceOfChildhood = "2-Significant problems",
      relCloseFamily = "1-Some problems",
      prevCloseRelationships = "2-Significant problems",
    ),
    ScoredSection.LifestyleAndAssociates(
      lifestyleLinkedToHarm = "Yes",
      lifestyleLinkedToReoffending = "Yes",
      regActivitiesEncourageOffending = "0-No problems",
      recklessness = "1-Some problems",
      easilyInfluenced = "1-Some problems",
    ),
    ScoredSection.DrugMisuse(
      drugLinkedToHarm = "Yes",
      drugLinkedToReoffending = "Yes",
      levelOfUseOfMainDrug = "1-Some problems",
      everInjectedDrugs = "Never",
      currentDrugNoted = "1-Some problems",
      motivationToTackleDrugMisuse = "0-No problems",
      drugsMajorActivity = "1-Some problems",
    ),
    ScoredSection.AlcoholMisuse(
      alcoholLinkedToHarm = "Yes",
      alcoholLinkedToReoffending = "No",
      bingeDrinking = "1-Some problems",
      currentUse = "1-Some problems",
      frequencyAndLevel = "1-Some problems",
      alcoholTackleMotivation = "2-Significant problemsø",
    ),
    ScoredSection.ThinkingAndBehaviour(
      thinkLinkedToHarm = "No",
      thinkLinkedToReoffending = "Yes",
      recogniseProblems = "1-Some problems",
      awarenessOfConsequences = "1-Some problems",
      understandsViewsOfOthers = "2-Significant problems",
      temperControlStr = "2-Significant problems",
      impulsivityStr = "2-Significant problems",
      problemSolvingSkills = "1-Some problems",
    ),
    ScoredSection.Attitudes(
      attLinkedToHarm = "No",
      attLinkedToReoffending = "Yes",
      proCriminalAttitudes = "1-Some problems",
      attitudesTowardsSupervision = "2-Significant problems",
      motivationToAddressBehaviour = "2-Significant problems",
      attitudesTowardsCommunitySociety = "2-Significant problems",
    ),
  )

  private fun offenderNeedsWithNotIdentified(assessmentSummary: BasicAssessmentSummary) = SectionSummary(
    assessmentSummary,
    ScoredSection.Accommodation(
      accLinkedToHarm = "Yes",
      accLinkedToReoffending = "Yes",
      noFixedAbodeOrTransient = "NO",
      suitabilityOfAccommodation = "1-Some problems",
      locationOfAccommodation = "2-Significant problems",
      permanenceOfAccommodation = "1-Some problems",
    ),
    ScoredSection.EducationTrainingEmployability(
      eTeLinkedToHarm = "Yes",
      eTeLinkedToReoffending = "Yes",
      unemployed = "0-No",
      workRelatedSkills = "0-No problems",
      employmentHistory = "2-Significant problems",
      attitudeToEmployment = "1-Some problems",
    ),
    ScoredSection.Relationships(
      relLinkedToHarm = "No",
      relLinkedToReoffending = "Yes",
      relParentalResponsibilities = "No",
      experienceOfChildhood = "2-Significant problems",
      relCloseFamily = "1-Some problems",
      prevCloseRelationships = "2-Significant problems",
    ),
    ScoredSection.LifestyleAndAssociates(
      lifestyleLinkedToHarm = "Yes",
      lifestyleLinkedToReoffending = "Yes",
      regActivitiesEncourageOffending = "0-No problems",
      recklessness = "1-Some problems",
      easilyInfluenced = "1-Some problems",
    ),
    ScoredSection.DrugMisuse(
      drugLinkedToHarm = "Yes",
      drugLinkedToReoffending = "Yes",
      levelOfUseOfMainDrug = "1-Some problems",
      everInjectedDrugs = "Never",
      currentDrugNoted = "1-Some problems",
      motivationToTackleDrugMisuse = "0-No problems",
      drugsMajorActivity = "1-Some problems",
    ),
    ScoredSection.AlcoholMisuse(
      alcoholLinkedToHarm = "Yes",
      alcoholLinkedToReoffending = "No",
      bingeDrinking = "1-Some problems",
      currentUse = "1-Some problems",
      frequencyAndLevel = "1-Some problems",
      alcoholTackleMotivation = "2-Significant problemsø",
    ),
    ScoredSection.ThinkingAndBehaviour(
      thinkLinkedToHarm = "No",
      thinkLinkedToReoffending = "No",
      recogniseProblems = "0-No problems",
      awarenessOfConsequences = "0-No problems",
      understandsViewsOfOthers = "0-No problems",
      temperControlStr = "0-No problems",
      impulsivityStr = "0-No problems",
      problemSolvingSkills = "0-No problems",
    ),
    ScoredSection.Attitudes(
      attLinkedToHarm = "No",
      attLinkedToReoffending = "No",
      proCriminalAttitudes = "0-No problems",
      attitudesTowardsSupervision = "0-No problems",
      motivationToAddressBehaviour = "0-No problems",
      attitudesTowardsCommunitySociety = "0-No problems",
    ),
  )

  private fun offenderNeedsWithUnanswered(assessmentSummary: BasicAssessmentSummary) = SectionSummary(
    assessmentSummary,
    ScoredSection.Accommodation(
      accLinkedToHarm = "Yes",
      accLinkedToReoffending = "Yes",
      noFixedAbodeOrTransient = "NO",
      suitabilityOfAccommodation = "1-Some problems",
      locationOfAccommodation = "2-Significant problems",
      permanenceOfAccommodation = "1-Some problems",
    ),
    ScoredSection.EducationTrainingEmployability(
      eTeLinkedToHarm = "Yes",
      eTeLinkedToReoffending = "Yes",
      unemployed = "0-No",
      workRelatedSkills = "0-No problems",
      employmentHistory = "2-Significant problems",
      attitudeToEmployment = "1-Some problems",
    ),
    ScoredSection.Relationships(
      relLinkedToHarm = "No",
      relLinkedToReoffending = "Yes",
      relParentalResponsibilities = "No",
      experienceOfChildhood = "2-Significant problems",
      relCloseFamily = "1-Some problems",
      prevCloseRelationships = "2-Significant problems",
    ),
    ScoredSection.LifestyleAndAssociates(
      lifestyleLinkedToHarm = "Yes",
      lifestyleLinkedToReoffending = "Yes",
      regActivitiesEncourageOffending = "0-No problems",
      recklessness = "1-Some problems",
      easilyInfluenced = "1-Some problems",
    ),
    ScoredSection.DrugMisuse(
      drugLinkedToHarm = "Yes",
      drugLinkedToReoffending = "Yes",
      levelOfUseOfMainDrug = "1-Some problems",
      everInjectedDrugs = "Never",
      currentDrugNoted = "1-Some problems",
      motivationToTackleDrugMisuse = "0-No problems",
      drugsMajorActivity = "1-Some problems",
    ),
    ScoredSection.AlcoholMisuse(
      alcoholLinkedToHarm = "Yes",
      alcoholLinkedToReoffending = "No",
      bingeDrinking = "1-Some problems",
      currentUse = "1-Some problems",
      frequencyAndLevel = "1-Some problems",
      alcoholTackleMotivation = "2-Significant problemsø",
    ),
    ScoredSection.ThinkingAndBehaviour(
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
    ),
    ScoredSection.Attitudes(null, null, null, null, null, null),
  )
}
