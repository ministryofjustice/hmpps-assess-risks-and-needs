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
        accommodation.copy(locationOfAccommodation = "2-Significant problems"),
        NeedSeverity.STANDARD,
      ),
      Arguments.of(
        accommodation.copy(
          locationOfAccommodation = "1-Some problems",
          suitabilityOfAccommodation = "1-Some problems",
          permanenceOfAccommodation = "2-Significant problems",
        ),
        NeedSeverity.STANDARD,
      ),
      Arguments.of(
        accommodation.copy(
          noFixedAbodeOrTransient = "Yes",
          locationOfAccommodation = "2-Significant problems",
          suitabilityOfAccommodation = "2-Significant problems",
          permanenceOfAccommodation = "1-Some problems",
        ),
        NeedSeverity.SEVERE,
      ),
      Arguments.of(ete, NeedSeverity.NO_NEED),
      Arguments.of(
        ete.copy(
          eTeLinkedToReoffending = "Yes",
          workRelatedSkills = "2-Significant problems",
          attitudeToEmployment = "1-Some problems",
        ),
        NeedSeverity.STANDARD,
      ),
      Arguments.of(
        ete.copy(
          attitudeToEmployment = "2-Significant problems",
          workRelatedSkills = "2-Significant problems",
          unemployed = "2-Yes",
        ),
        NeedSeverity.STANDARD,
      ),
      Arguments.of(
        ete.copy(
          workRelatedSkills = "2-Significant problems",
          unemployed = "2-Yes",
          attitudeToEmployment = "1-Some problems",
          employmentHistory = "2-Significant problems",
        ),
        NeedSeverity.SEVERE,
      ),
      Arguments.of(relationships, NeedSeverity.NO_NEED),
      Arguments.of(
        relationships.copy(relCloseFamily = "2-Significant problems"),
        NeedSeverity.STANDARD,
      ),
      Arguments.of(
        relationships.copy(
          relLinkedToReoffending = "Yes",
          experienceOfChildhood = "1-Some problems",
          relCloseFamily = "1-Some problems",
          prevCloseRelationships = "2-Significant problems",
          relParentalResponsibilities = "Yes", // should not affect score
        ),
        NeedSeverity.STANDARD,
      ),
      Arguments.of(
        relationships.copy(
          experienceOfChildhood = "2-Significant problems",
          relCloseFamily = "1-Some problems",
          prevCloseRelationships = "2-Significant problems",
        ),
        NeedSeverity.SEVERE,
      ),
      Arguments.of(lifestyle, NeedSeverity.NO_NEED),
      Arguments.of(
        lifestyle.copy(regActivitiesEncourageOffending = "2-Significant Problems"),
        NeedSeverity.STANDARD,
      ),
      Arguments.of(
        lifestyle.copy(
          lifestyleLinkedToReoffending = "Yes",
          regActivitiesEncourageOffending = "2-Significant Problems",
          easilyInfluenced = "2-Significant Problems",
        ),
        NeedSeverity.STANDARD,
      ),
      Arguments.of(
        lifestyle.copy(
          recklessness = "2-Significant Problems",
          regActivitiesEncourageOffending = "2-Significant Problems",
          easilyInfluenced = "1-Some problems",
        ),
        NeedSeverity.SEVERE,
      ),
      Arguments.of(drugMisuse, NeedSeverity.NO_NEED),
      Arguments.of(
        drugMisuse.copy(everInjectedDrugs = "Currently"),
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
          levelOfUseOfMainDrug = "2-Significant Problems",
          everInjectedDrugs = "Currently",
          currentDrugNoted = "2-Significant Problems",
          motivationToTackleDrugMisuse = "2-Significant Problems",
        ),
        NeedSeverity.SEVERE,
      ),
      Arguments.of(alcoholMisuse, NeedSeverity.NO_NEED),
      Arguments.of(
        alcoholMisuse.copy(currentUse = "2-Significant problems", frequencyAndLevel = "1-Some problems"),
        NeedSeverity.NO_NEED,
      ),
      Arguments.of(
        alcoholMisuse.copy(currentUse = "2-Significant problems", frequencyAndLevel = "2-Significant problems"),
        NeedSeverity.STANDARD,
      ),
      Arguments.of(
        alcoholMisuse.copy(
          frequencyAndLevel = "2-Significant problems",
          currentUse = "1-Some problems",
          bingeDrinking = "2-Significant problems",
          alcoholTackleMotivation = "2-Significant problems",
        ),
        NeedSeverity.SEVERE,
      ),
      Arguments.of(thinking, NeedSeverity.NO_NEED),
      Arguments.of(
        thinking.copy(
          thinkLinkedToReoffending = "Yes",
          problemSolvingSkills = "2-Significant problems",
          recogniseProblems = "1-Significant problems",
        ),
        NeedSeverity.NO_NEED,
      ),
      Arguments.of(
        thinking.copy(
          recogniseProblems = "2-Significant problems",
          problemSolvingSkills = "1-Some problems",
          impulsivityStr = "1-Some problems", // shouldn't affect score
          temperControlStr = "1-Some problems", // shouldn't affect score
        ),
        NeedSeverity.NO_NEED,
      ),
      Arguments.of(
        thinking.copy(
          recogniseProblems = "2-Significant problems",
          problemSolvingSkills = "2-Significant problems",
          understandsViewsOfOthers = "2-Significant problems",
          awarenessOfConsequences = "1-Some problems",
        ),
        NeedSeverity.SEVERE,
      ),
      Arguments.of(attitude, NeedSeverity.NO_NEED),
      Arguments.of(
        attitude.copy(
          proCriminalAttitudes = "1-Some problems",
          attitudesTowardsSupervision = "1-Some problems",
        ),
        NeedSeverity.STANDARD,
      ),
      Arguments.of(
        attitude.copy(
          attLinkedToReoffending = "Yes",
          attitudesTowardsSupervision = "2-Significant problems",
          motivationToAddressBehaviour = "2-Significant problems",
          attitudesTowardsCommunitySociety = "2-Significant problems",
        ),
        NeedSeverity.STANDARD,
      ),
      Arguments.of(
        attitude.copy(
          proCriminalAttitudes = "1-Some problems",
          attitudesTowardsSupervision = "2-Significant problems",
          motivationToAddressBehaviour = "2-Significant problems",
          attitudesTowardsCommunitySociety = "2-Significant problems",
        ),
        NeedSeverity.SEVERE,
      ),
    )
  }
}
