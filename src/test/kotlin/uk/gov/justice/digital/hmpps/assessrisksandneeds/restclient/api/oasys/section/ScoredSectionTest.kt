package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.oasys.section

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.NeedSeverity

class ScoredSectionTest {

  @ParameterizedTest
  @MethodSource("scoredSections")
  fun `correctly calculates the need severity`(scoredSection: ScoredSection, needSeverity: NeedSeverity) {
    assertThat(scoredSection.severity, equalTo(needSeverity))
  }

  companion object {
    val accommodation = ScoredSection.Accommodation(
      "No",
      "No",
      "No",
      "0-No problems",
      "0-No problems",
      "0-No problems",
    )

    val ete = ScoredSection.EducationTrainingEmployment(
      "No",
      "No",
      "0-No",
      "0-No problems",
      "0-No problems",
      "0-No problems",
    )

    val relationships = ScoredSection.Relationships(
      "No",
      "No",
      "0-No problems",
      "0-No problems",
      "0-No problems",
      "0-No problems",
    )

    val lifestyle = ScoredSection.LifestyleAndAssociates(
      "No",
      "No",
      "0-No problems",
      "0-No problems",
      "0-No problems",
    )

    val drugMisuse = ScoredSection.DrugMisuse(
      "No",
      "No",
      "0-No problems",
      "0-No problems",
      "Never",
      "0-No problems",
      "0-No problems",
    )

    val alcoholMisuse = ScoredSection.AlcoholMisuse(
      "No",
      "No",
      "0-No problems",
      "0-No problems",
      "Never",
      "0-No problems",
    )

    val thinking = ScoredSection.ThinkingAndBehaviour(
      "No",
      "No",
      "0-No problems",
      "0-No problems",
      "0-No problems",
      "0-No problems",
      "0-No problems",
      "0-No problems",
    )

    val attitude = ScoredSection.Attitudes(
      "No",
      "No",
      "0-No problems",
      "0-No problems",
      "0-No problems",
      "0-No problems",
    )

    @JvmStatic
    fun scoredSections() = listOf(
      Arguments.of(accommodation, NeedSeverity.NO_NEED),
      Arguments.of(
        accommodation.copy(accLinkedToReoffending = "Yes", locationOfAccommodation = "1-Some problems"),
        NeedSeverity.STANDARD,
      ),
      Arguments.of(
        accommodation.copy(
          accLinkedToHarm = "Yes",
          locationOfAccommodation = "1-Some problems",
          suitabilityOfAccommodation = "1-Some problems",
          permanenceOfAccommodation = "1-Some problems",
        ),
        NeedSeverity.STANDARD,
      ),
      Arguments.of(
        accommodation.copy(
          accLinkedToReoffending = "Yes",
          locationOfAccommodation = "2-Significant problems",
          suitabilityOfAccommodation = "2-Significant problems",
          permanenceOfAccommodation = "1-Some problems",
        ),
        NeedSeverity.SEVERE,
      ),
      Arguments.of(ete, NeedSeverity.NO_NEED),
      Arguments.of(
        ete.copy(eTeLinkedToReoffending = "Yes", workRelatedSkills = "1-Some problems"),
        NeedSeverity.STANDARD,
      ),
      Arguments.of(
        ete.copy(eTeLinkedToHarm = "Yes", workRelatedSkills = "2-Some problems", unemployed = "2-Yes"),
        NeedSeverity.STANDARD,
      ),
      Arguments.of(
        ete.copy(
          eTeLinkedToHarm = "Yes",
          workRelatedSkills = "2-Some problems",
          unemployed = "2-Yes",
          attitudeToEmployment = "1-Some problems",
        ),
        NeedSeverity.SEVERE,
      ),
      Arguments.of(relationships, NeedSeverity.NO_NEED),
      Arguments.of(
        relationships.copy(relLinkedToReoffending = "Yes"),
        NeedSeverity.STANDARD,
      ),
      Arguments.of(
        relationships.copy(
          relLinkedToReoffending = "Yes",
          experienceOfChildhood = "1-Some problems",
          relCloseFamily = "1-Some problems",
          relParentalResponsibilities = "Yes", // should not affect score
        ),
        NeedSeverity.STANDARD,
      ),
      Arguments.of(
        relationships.copy(
          relLinkedToReoffending = "Yes",
          experienceOfChildhood = "1-Some problems",
          relCloseFamily = "1-Some problems",
          prevCloseRelationships = "1-Some problems",
        ),
        NeedSeverity.SEVERE,
      ),
      Arguments.of(lifestyle, NeedSeverity.NO_NEED),
      Arguments.of(
        lifestyle.copy(lifestyleLinkedToReoffending = "Yes"),
        NeedSeverity.STANDARD,
      ),
      Arguments.of(
        lifestyle.copy(
          lifestyleLinkedToReoffending = "Yes",
          regActivitiesEncourageOffending = "2-Significant Problems",
        ),
        NeedSeverity.STANDARD,
      ),
      Arguments.of(
        lifestyle.copy(
          lifestyleLinkedToReoffending = "Yes",
          regActivitiesEncourageOffending = "2-Significant Problems",
          easilyInfluenced = "1-Some problems",
        ),
        NeedSeverity.SEVERE,
      ),
      Arguments.of(drugMisuse, NeedSeverity.NO_NEED),
      Arguments.of(
        drugMisuse.copy(drugLinkedToReoffending = "Yes"),
        NeedSeverity.STANDARD,
      ),
      Arguments.of(
        drugMisuse.copy(
          drugLinkedToReoffending = "Yes",
          drugLinkedToHarm = "Yes",
          levelOfUseOfMainDrug = "2-Significant Problems",
          everInjectedDrugs = "Previously",
        ),
        NeedSeverity.STANDARD,
      ),
      Arguments.of(
        drugMisuse.copy(
          drugLinkedToReoffending = "Yes",
          drugLinkedToHarm = "Yes",
          levelOfUseOfMainDrug = "2-Significant Problems",
          everInjectedDrugs = "Currently",
        ),
        NeedSeverity.SEVERE,
      ),
      Arguments.of(alcoholMisuse, NeedSeverity.NO_NEED),
      Arguments.of(
        alcoholMisuse.copy(alcoholLinkedToReoffending = "Yes", currentUse = "1-Some problems"),
        NeedSeverity.NO_NEED,
      ),
      Arguments.of(
        alcoholMisuse.copy(alcoholLinkedToReoffending = "Yes", frequencyAndLevel = "2-Significant problems"),
        NeedSeverity.STANDARD,
      ),
      Arguments.of(
        alcoholMisuse.copy(
          alcoholLinkedToReoffending = "Yes",
          frequencyAndLevel = "2-Significant problems",
          currentUse = "1-Some problems",
          bingeDrinking = "2-Significant problems",
          alcoholTackleMotivation = "1-Some problems",
        ),
        NeedSeverity.SEVERE,
      ),
      Arguments.of(thinking, NeedSeverity.NO_NEED),
      Arguments.of(
        thinking.copy(thinkLinkedToReoffending = "Yes", problemSolvingSkills = "1-Some problems"),
        NeedSeverity.NO_NEED,
      ),
      Arguments.of(
        thinking.copy(
          thinkLinkedToHarm = "Yes",
          recogniseProblems = "1-Some problems",
          impulsivityStr = "1-Some problems", // shouldn't affect score
          temperControlStr = "1-Some problems", // shouldn't affect score
        ),
        NeedSeverity.NO_NEED,
      ),
      Arguments.of(
        thinking.copy(
          thinkLinkedToReoffending = "Yes",
          recogniseProblems = "1-Some problems",
          problemSolvingSkills = "2-Some problems",
          understandsViewsOfOthers = "1-Some problems",
          awarenessOfConsequences = "1-Some problems",
        ),
        NeedSeverity.SEVERE,
      ),
      Arguments.of(attitude, NeedSeverity.NO_NEED),
      Arguments.of(
        attitude.copy(attLinkedToReoffending = "Yes"),
        NeedSeverity.STANDARD,
      ),
      Arguments.of(
        attitude.copy(
          attLinkedToReoffending = "Yes",
          proCriminalAttitudes = "2-Significant problems",
          attitudesTowardsSupervision = "2-Significant problems",
        ),
        NeedSeverity.STANDARD,
      ),
      Arguments.of(
        attitude.copy(
          attLinkedToReoffending = "Yes",
          proCriminalAttitudes = "1-Some problems",
          attitudesTowardsSupervision = "2-Significant problems",
          motivationToAddressBehaviour = "1-Some problems",
          attitudesTowardsCommunitySociety = "1-Some problems",
        ),
        NeedSeverity.SEVERE,
      ),
    )
  }
}
