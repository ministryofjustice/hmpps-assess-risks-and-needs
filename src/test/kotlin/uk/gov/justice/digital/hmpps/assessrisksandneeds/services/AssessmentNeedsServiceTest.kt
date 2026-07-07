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
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AccNeeds
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AccomSanNeeds
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AlcoUseSanNeeds
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AlcoholNeeds
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentSection
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentVersion
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AttNeeds
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.BasicAssessmentSummary
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.CriminogenicNeedsAssessmentOasys
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.CriminogenicNeedsOasys
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.DrugNeeds
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.DrugUseSanNeeds
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ETENeeds
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.EmoNeeds
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.EmpAndEduSanNeeds
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.FinanceNeeds
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.FinanceSanNeeds
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.HealthAndWellbeingSanNeeds
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.LifeAndAssocSanNeeds
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.LifestyleNeeds
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.NeedStatus
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PersRelAndCommSanNeeds
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PersonIdentifier
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RelNeeds
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.SanNeeds
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ThinkBehavAndAttiSanNeeds
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.ThinkNeeds
import uk.gov.justice.digital.hmpps.assessrisksandneeds.config.Clock
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.OasysApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.oasys.section.OasysThreshold
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.EntityNotFoundException
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
@DisplayName("Assessment Need Service Tests")
class AssessmentNeedsServiceTest {
  private val oasysApiRestClient: OasysApiRestClient = mockk()
  private val clock: Clock = mockk()
  private val assessmentNeedsService = AssessmentNeedsService(oasysApiRestClient, clock)

  @Test
  fun `should bucket OASYS needs by score against threshold`() {
    // Arrange
    val identifier = PersonIdentifier(PersonIdentifier.Type.CRN, "T123456")
    val assessment = BasicAssessmentSummary(9630348, LocalDateTime.parse("2024-12-25T12:00:00"), LocalDateTime.parse("2024-12-25T12:00:00"), "LAYER3", "COMPLETE")
    every { oasysApiRestClient.getLatestAssessment(eq(identifier), any()) } answers { assessment }
    every { oasysApiRestClient.getCriminogenicNeedsForAssessment(assessment) } answers { oasysNeeds(assessment.assessmentId) }

    // Act
    val needs = assessmentNeedsService.getAssessmentNeeds(identifier.value)

    // Assert
    assertThat(needs.assessmentVersion).isEqualTo(AssessmentVersion.OASYS)
    assertThat(needs.assessedOn).isEqualTo(LocalDateTime.parse("2024-12-19T16:57:25"))
    assertThat(needs.identifiedNeeds.map { it.section }).containsExactlyInAnyOrder(
      AssessmentSection.ACCOMMODATION.name,
      AssessmentSection.LIFESTYLE_AND_ASSOCIATES.name,
      AssessmentSection.THINKING_AND_BEHAVIOUR.name,
    )
    assertThat(needs.notIdentifiedNeeds.map { it.section }).containsExactlyInAnyOrder(
      AssessmentSection.EDUCATION_TRAINING_AND_EMPLOYABILITY.name,
      AssessmentSection.DRUG_MISUSE.name,
      AssessmentSection.ATTITUDE.name,
    )
    assertThat(needs.unansweredNeeds.map { it.section }).containsExactlyInAnyOrder(
      AssessmentSection.RELATIONSHIPS.name,
      AssessmentSection.ALCOHOL_MISUSE.name,
    )
  }

  @Test
  fun `should map OASYS section fields onto the need`() {
    // Arrange
    val identifier = PersonIdentifier(PersonIdentifier.Type.CRN, "T123456")
    val assessment = BasicAssessmentSummary(9630348, LocalDateTime.parse("2024-12-25T12:00:00"), LocalDateTime.parse("2024-12-25T12:00:00"), "LAYER3", "COMPLETE")
    every { oasysApiRestClient.getLatestAssessment(eq(identifier), any()) } answers { assessment }
    every { oasysApiRestClient.getCriminogenicNeedsForAssessment(assessment) } answers { oasysNeeds(assessment.assessmentId) }

    // Act
    val needs = assessmentNeedsService.getAssessmentNeeds(identifier.value)

    // Assert
    val accommodation = needs.identifiedNeeds.single { it.section == AssessmentSection.ACCOMMODATION.name }
    assertThat(accommodation.name).isEqualTo("Accommodation")
    assertThat(accommodation.riskOfHarm).isTrue()
    assertThat(accommodation.riskOfReoffending).isFalse()
    assertThat(accommodation.score).isEqualTo(5)
    assertThat(accommodation.oasysThreshold).isEqualTo(OasysThreshold(2))
  }

  @Test
  fun `should map a SAN assessment using sanCrimNeedScore`() {
    // Arrange
    val identifier = PersonIdentifier(PersonIdentifier.Type.CRN, "T123456")
    val assessment = BasicAssessmentSummary(9700001, LocalDateTime.parse("2024-12-25T12:00:00"), LocalDateTime.parse("2024-12-25T12:00:00"), "LAYER3", "COMPLETE")
    every { oasysApiRestClient.getLatestAssessment(eq(identifier), any()) } answers { assessment }
    every { oasysApiRestClient.getCriminogenicNeedsForAssessment(assessment) } answers { sanNeeds(assessment.assessmentId) }

    // Act
    val needs = assessmentNeedsService.getAssessmentNeeds(identifier.value)

    // Assert
    assertThat(needs.assessmentVersion).isEqualTo(AssessmentVersion.SAN)
    assertThat(needs.identifiedNeeds.map { it.section }).containsExactlyInAnyOrder(
      AssessmentSection.EMPLOYMENT_AND_EDUCATION.name,
      AssessmentSection.PERSONAL_RELATIONSHIPS_AND_COMMUNITY.name,
      AssessmentSection.THINKING_ATTITUDES_AND_BEHAVIOUR.name,
    )
    assertThat(needs.notIdentifiedNeeds.map { it.section }).containsExactlyInAnyOrder(
      AssessmentSection.ACCOMMODATION.name,
      AssessmentSection.LIFESTYLE_AND_ASSOCIATES.name,
      AssessmentSection.ALCOHOL_USE.name,
    )
    assertThat(needs.unansweredNeeds.map { it.section }).containsExactly(AssessmentSection.DRUG_USE.name)
  }

  @Test
  fun `should set risk of harm and reoffending to null for SAN lifestyle and associates`() {
    // Arrange
    val identifier = PersonIdentifier(PersonIdentifier.Type.CRN, "T123456")
    val assessment = BasicAssessmentSummary(9700001, LocalDateTime.parse("2024-12-25T12:00:00"), LocalDateTime.parse("2024-12-25T12:00:00"), "LAYER3", "COMPLETE")
    every { oasysApiRestClient.getLatestAssessment(eq(identifier), any()) } answers { assessment }
    every { oasysApiRestClient.getCriminogenicNeedsForAssessment(assessment) } answers { sanNeeds(assessment.assessmentId) }

    // Act
    val needs = assessmentNeedsService.getAssessmentNeeds(identifier.value)

    // Assert
    val lifestyle = needs.notIdentifiedNeeds.single { it.section == AssessmentSection.LIFESTYLE_AND_ASSOCIATES.name }
    assertThat(lifestyle.name).isEqualTo("Lifestyle and associates")
    assertThat(lifestyle.riskOfHarm).isNull()
    assertThat(lifestyle.riskOfReoffending).isNull()
  }

  @Test
  fun `should return all OASYS need details ordered by need status with finance and emotional wellbeing unscored`() {
    // Arrange
    val identifier = PersonIdentifier(PersonIdentifier.Type.CRN, "T123456")
    val assessment = BasicAssessmentSummary(9630348, LocalDateTime.parse("2024-12-25T12:00:00"), LocalDateTime.parse("2024-12-25T12:00:00"), "LAYER3", "COMPLETE")
    every { oasysApiRestClient.getLatestAssessment(eq(identifier), any()) } answers { assessment }
    every { oasysApiRestClient.getCriminogenicNeedsForAssessment(assessment) } answers { oasysNeeds(assessment.assessmentId) }

    // Act
    val needs = assessmentNeedsService.getAssessmentNeedsDetails(identifier.value)

    // Assert
    assertThat(needs.assessmentVersion).isEqualTo(AssessmentVersion.OASYS)
    assertThat(needs.assessedOn).isEqualTo(LocalDateTime.parse("2024-12-19T16:57:25"))
    assertThat(needs.needs.map { it.section to it.needStatus }).containsExactly(
      AssessmentSection.ACCOMMODATION.name to NeedStatus.IDENTIFIED_NEED,
      AssessmentSection.LIFESTYLE_AND_ASSOCIATES.name to NeedStatus.IDENTIFIED_NEED,
      AssessmentSection.THINKING_AND_BEHAVIOUR.name to NeedStatus.IDENTIFIED_NEED,
      AssessmentSection.EDUCATION_TRAINING_AND_EMPLOYABILITY.name to NeedStatus.NOT_IDENTIFIED_NEED,
      AssessmentSection.DRUG_MISUSE.name to NeedStatus.NOT_IDENTIFIED_NEED,
      AssessmentSection.ATTITUDE.name to NeedStatus.NOT_IDENTIFIED_NEED,
      AssessmentSection.RELATIONSHIPS.name to NeedStatus.UNANSWERED_NEED,
      AssessmentSection.ALCOHOL_MISUSE.name to NeedStatus.UNANSWERED_NEED,
      AssessmentSection.FINANCE.name to NeedStatus.UNSCORED_NEED,
      AssessmentSection.EMOTIONAL_WELLBEING.name to NeedStatus.UNSCORED_NEED,
    )

    val finance = needs.needs.single { it.section == AssessmentSection.FINANCE.name }
    assertThat(finance.name).isEqualTo("Finance")
    assertThat(finance.score).isNull()
    assertThat(finance.oasysThreshold).isEqualTo(OasysThreshold(null))
    assertThat(finance.riskOfHarm).isFalse()
    assertThat(finance.riskOfReoffending).isFalse()
  }

  @Test
  fun `should return all SAN need details with finance and health and wellbeing unscored`() {
    // Arrange
    val identifier = PersonIdentifier(PersonIdentifier.Type.CRN, "T123456")
    val assessment = BasicAssessmentSummary(9700001, LocalDateTime.parse("2024-12-25T12:00:00"), LocalDateTime.parse("2024-12-25T12:00:00"), "LAYER3", "COMPLETE")
    every { oasysApiRestClient.getLatestAssessment(eq(identifier), any()) } answers { assessment }
    every { oasysApiRestClient.getCriminogenicNeedsForAssessment(assessment) } answers { sanNeeds(assessment.assessmentId) }

    // Act
    val needs = assessmentNeedsService.getAssessmentNeedsDetails(identifier.value)

    // Assert
    assertThat(needs.assessmentVersion).isEqualTo(AssessmentVersion.SAN)
    assertThat(needs.needs.map { it.section to it.needStatus }).containsExactly(
      AssessmentSection.EMPLOYMENT_AND_EDUCATION.name to NeedStatus.IDENTIFIED_NEED,
      AssessmentSection.PERSONAL_RELATIONSHIPS_AND_COMMUNITY.name to NeedStatus.IDENTIFIED_NEED,
      AssessmentSection.THINKING_ATTITUDES_AND_BEHAVIOUR.name to NeedStatus.IDENTIFIED_NEED,
      AssessmentSection.ACCOMMODATION.name to NeedStatus.NOT_IDENTIFIED_NEED,
      AssessmentSection.LIFESTYLE_AND_ASSOCIATES.name to NeedStatus.NOT_IDENTIFIED_NEED,
      AssessmentSection.ALCOHOL_USE.name to NeedStatus.NOT_IDENTIFIED_NEED,
      AssessmentSection.DRUG_USE.name to NeedStatus.UNANSWERED_NEED,
      AssessmentSection.FINANCE.name to NeedStatus.UNSCORED_NEED,
      AssessmentSection.HEALTH_AND_WELLBEING.name to NeedStatus.UNSCORED_NEED,
    )

    val health = needs.needs.single { it.section == AssessmentSection.HEALTH_AND_WELLBEING.name }
    assertThat(health.name).isEqualTo("Health and wellbeing")
    assertThat(health.score).isNull()
    assertThat(health.oasysThreshold).isEqualTo(OasysThreshold(null))
    assertThat(health.riskOfHarm).isNull()
    assertThat(health.riskOfReoffending).isNull()
  }

  @Test
  fun `should throw exception when no latest assessment found`() {
    // Arrange
    val identifier = PersonIdentifier(PersonIdentifier.Type.CRN, "N123456")
    every { oasysApiRestClient.getLatestAssessment(eq(identifier), any()) } answers { null }

    // Act
    val exception = assertThrows<EntityNotFoundException> {
      assessmentNeedsService.getAssessmentNeeds(identifier.value)
    }

    // Assert
    assertEquals("No needs found for CRN: ${identifier.value}", exception.message)
  }

  @Test
  fun `should throw exception when criminogenic needs not found`() {
    // Arrange
    val identifier = PersonIdentifier(PersonIdentifier.Type.CRN, "N123456")
    val assessment = BasicAssessmentSummary(9630348, LocalDateTime.parse("2024-12-25T12:00:00"), LocalDateTime.parse("2024-12-25T12:00:00"), "LAYER3", "COMPLETE")
    every { oasysApiRestClient.getLatestAssessment(eq(identifier), any()) } answers { assessment }
    every { oasysApiRestClient.getCriminogenicNeedsForAssessment(assessment) } answers { null }

    // Act
    val exception = assertThrows<EntityNotFoundException> {
      assessmentNeedsService.getAssessmentNeeds(identifier.value)
    }

    // Assert
    assertEquals("No needs found for CRN: ${identifier.value}", exception.message)
  }

  @Test
  fun `should throw when assessment version is unrecognised`() {
    // Arrange
    val identifier = PersonIdentifier(PersonIdentifier.Type.CRN, "T123456")
    val assessment = BasicAssessmentSummary(9630348, LocalDateTime.parse("2024-12-25T12:00:00"), LocalDateTime.parse("2024-12-25T12:00:00"), "LAYER3", "COMPLETE")
    val needs = CriminogenicNeedsOasys(listOf(CriminogenicNeedsAssessmentOasys(assessmentPk = assessment.assessmentId, assessmentVersion = "3")))
    every { oasysApiRestClient.getLatestAssessment(eq(identifier), any()) } answers { assessment }
    every { oasysApiRestClient.getCriminogenicNeedsForAssessment(assessment) } answers { needs }

    // Act & Assert
    assertThrows<IllegalStateException> {
      assessmentNeedsService.getAssessmentNeeds(identifier.value)
    }
  }

  @Test
  fun `should throw exception when no returned assessment matches the latest assessment`() {
    // Arrange
    val identifier = PersonIdentifier(PersonIdentifier.Type.CRN, "T123456")
    val assessment = BasicAssessmentSummary(9630348, LocalDateTime.parse("2024-12-25T12:00:00"), LocalDateTime.parse("2024-12-25T12:00:00"), "LAYER3", "COMPLETE")
    val needs = CriminogenicNeedsOasys(
      listOf(
        CriminogenicNeedsAssessmentOasys(assessmentPk = 111, assessmentVersion = "1"),
        CriminogenicNeedsAssessmentOasys(assessmentPk = 222, assessmentVersion = "1"),
      ),
    )
    every { oasysApiRestClient.getLatestAssessment(eq(identifier), any()) } answers { assessment }
    every { oasysApiRestClient.getCriminogenicNeedsForAssessment(assessment) } answers { needs }

    // Act
    val exception = assertThrows<EntityNotFoundException> {
      assessmentNeedsService.getAssessmentNeeds(identifier.value)
    }

    // Assert
    assertEquals("No needs found for CRN: ${identifier.value}", exception.message)
  }

  @Test
  fun `should use the only returned assessment when its pk does not match the latest assessment`() {
    // Arrange
    val identifier = PersonIdentifier(PersonIdentifier.Type.CRN, "T123456")
    val assessment = BasicAssessmentSummary(9630348, LocalDateTime.parse("2024-12-25T12:00:00"), LocalDateTime.parse("2024-12-25T12:00:00"), "LAYER3", "COMPLETE")
    every { oasysApiRestClient.getLatestAssessment(eq(identifier), any()) } answers { assessment }
    every { oasysApiRestClient.getCriminogenicNeedsForAssessment(assessment) } answers { oasysNeeds(assessmentPk = 999) }

    // Act
    val needs = assessmentNeedsService.getAssessmentNeeds(identifier.value)

    // Assert
    assertThat(needs.assessmentVersion).isEqualTo(AssessmentVersion.OASYS)
    assertThat(needs.identifiedNeeds).isNotEmpty()
  }

  private fun oasysNeeds(assessmentPk: Long) = CriminogenicNeedsOasys(
    assessments = listOf(
      CriminogenicNeedsAssessmentOasys(
        assessmentPk = assessmentPk,
        assessmentVersion = "1",
        dateCompleted = LocalDateTime.parse("2024-12-19T16:57:25"),
        acc = AccNeeds(accThreshold = 2, accLinkedToHarm = "Yes", accLinkedToReoffending = "No", accOtherWeightedScore = 5),
        eTE = ETENeeds(eTEThreshold = 3, eTELinkedToHarm = "No", eTELinkedToReoffending = "No", eTEOtherWeightedScore = 1),
        rel = RelNeeds(relThreshold = 2, relLinkedToHarm = "No", relLinkedToReoffending = "No", relOtherWeightedScore = null),
        lifestyle = LifestyleNeeds(lifestyleThreshold = 2, lifestyleLinkedToHarm = "Yes", lifestyleLinkedToReoffending = "Yes", lifestyleOtherWeightedScore = 2),
        drug = DrugNeeds(drugThreshold = 2, drugLinkedToHarm = "No", drugLinkedToReoffending = "No", drugOtherWeightedScore = 0),
        alcohol = AlcoholNeeds(alcoholThreshold = 4, alcoholLinkedToHarm = "No", alcoholLinkedToReoffending = "No", alcoholOtherWeightedScore = null),
        think = ThinkNeeds(thinkThreshold = 4, thinkLinkedToHarm = "Yes", thinkLinkedToReoffending = "Yes", thinkOtherWeightedScore = 8),
        att = AttNeeds(attThreshold = 2, attLinkedToHarm = "No", attLinkedToReoffending = "No", attOtherWeightedScore = 1),
        finance = FinanceNeeds(financeThreshold = null, financeLinkedToHarm = "No", financeLinkedToReoffending = "No", financeOtherWeightedScore = null),
        emo = EmoNeeds(emoThreshold = null, emoLinkedToHarm = "No", emoLinkedToReoffending = "No", emoOtherWeightedScore = null),
      ),
    ),
  )

  private fun sanNeeds(assessmentPk: Long) = CriminogenicNeedsOasys(
    assessments = listOf(
      CriminogenicNeedsAssessmentOasys(
        assessmentPk = assessmentPk,
        assessmentVersion = "2",
        dateCompleted = LocalDateTime.parse("2024-12-20T10:00:00"),
        sanCrimNeedScore = SanNeeds(
          accomSan = AccomSanNeeds(accomSanThreshold = 2, accomSanLinkedToHarm = "No", accomSanLinkedToReoffending = "No", accomSanScore = 1),
          empAndEduSan = EmpAndEduSanNeeds(empAndEduSanThreshold = 2, empAndEduSanLinkedToHarm = "No", empAndEduSanLinkedToReoffending = "No", empAndEduSanScore = 4),
          persRelAndCommSan = PersRelAndCommSanNeeds(persRelAndCommSanThreshold = 2, persRelAndCommSanLinkedToHarm = "No", persRelAndCommSanLinkedToReoffending = "No", persRelAndCommSanScore = 3),
          lifeAndAssocSan = LifeAndAssocSanNeeds(lifeAndAssocSanThreshold = 2, lifeAndAssocSanScore = 0),
          drugUseSan = DrugUseSanNeeds(drugUseSanThreshold = 2, drugUseSanLinkedToHarm = "No", drugUseSanLinkedToReoffending = "No", drugUseSanScore = null),
          alcoUseSan = AlcoUseSanNeeds(alcoUseSanThreshold = 2, alcoUseSanLinkedToHarm = "No", alcoUseSanLinkedToReoffending = "No", alcoUseSanScore = 0),
          thinkBehavAndAttiSan = ThinkBehavAndAttiSanNeeds(thinkBehavAndAttiSanThreshold = 2, thinkBehavAndAttiSanLinkedToHarm = "No", thinkBehavAndAttiSanLinkedToReoffending = "No", thinkBehavAndAttiSanScore = 6),
          financeSan = FinanceSanNeeds(financeSanThreshold = null, financeSanLinkedToHarm = "No", financeSanLinkedToReoffending = "No", financeSanScore = null),
          healthAndWellbeingSan = HealthAndWellbeingSanNeeds(),
        ),
      ),
    ),
  )
}
