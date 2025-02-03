package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.oasys.section

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonIgnore
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.NeedSeverity
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.oasys.section.ScoredAnswer.YesNo
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.NeedsSection

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
data class ScoredSectionResponse<T : ScoredSection>(
  private val assessments: List<T>,
) {
  val section: T? = assessments.firstOrNull()
}

@JsonAutoDetect(
  creatorVisibility = JsonAutoDetect.Visibility.ANY,
  getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
)
sealed interface ScoredSection {
  @get:JsonIgnore
  val section: NeedsSection
  val linkedToReOffending: YesNo
  val linkedToHarm: YesNo

  val oasysThreshold: OasysThreshold

  val tierThreshold: TierThreshold

  val questionAnswers: Map<String, ScoredAnswer>

  fun getScore(): Int? = if (questionAnswers.values.all { it == ScoredAnswer.Problem.Missing || it == ScoredAnswer.YesNo.Unknown }) {
    null
  } else {
    questionAnswers.values.sumOf { it.score }
  }

  fun getSeverity(): NeedSeverity? = when {
    getScore() == null -> null
    getScore()!! >= tierThreshold.severe -> NeedSeverity.SEVERE
    getScore()!! >= tierThreshold.standard -> NeedSeverity.STANDARD
    else -> NeedSeverity.NO_NEED
  }

  data class Accommodation(
    private val accLinkedToReoffending: String?,
    private val accLinkedToHarm: String?,
    private val noFixedAbodeOrTransient: String?,
    private val suitabilityOfAccommodation: String?,
    private val permanenceOfAccommodation: String?,
    private val locationOfAccommodation: String?,
  ) : ScoredSection {
    override val section = NeedsSection.ACCOMMODATION
    override val linkedToReOffending = YesNo.of(accLinkedToReoffending)
    override val linkedToHarm = YesNo.of(accLinkedToHarm)
    override val questionAnswers: Map<String, ScoredAnswer> = buildList {
      addAll(
        listOf(::suitabilityOfAccommodation, ::permanenceOfAccommodation, ::locationOfAccommodation)
          .map { it.name to ScoredAnswer.Problem.of(it.get()) },
      )
      add(::noFixedAbodeOrTransient.name to YesNo.of(noFixedAbodeOrTransient))
    }.toMap()
    override val tierThreshold = TierThreshold(2, 7)
    override val oasysThreshold = OasysThreshold(2)
  }

  data class EducationTrainingEmployability(
    @JsonAlias("eTELinkedToReoffending")
    private val eTeLinkedToReoffending: String?,
    @JsonAlias("eTELinkedToHarm")
    private val eTeLinkedToHarm: String?,
    private val unemployed: String?,
    private val employmentHistory: String?,
    private val workRelatedSkills: String?,
    private val attitudeToEmployment: String?,
  ) : ScoredSection {
    override val section = NeedsSection.EDUCATION_TRAINING_AND_EMPLOYABILITY
    override val linkedToReOffending = YesNo.of(eTeLinkedToReoffending)
    override val linkedToHarm = YesNo.of(eTeLinkedToHarm)
    override val questionAnswers: Map<String, ScoredAnswer> = listOf(
      ::unemployed,
      ::employmentHistory,
      ::workRelatedSkills,
      ::attitudeToEmployment,
    ).associate { it.name to ScoredAnswer.Problem.of(it.get()) }
    override val tierThreshold = TierThreshold(3, 7)
    override val oasysThreshold = OasysThreshold(3)
  }

  data class Relationships(
    private val relLinkedToReoffending: String?,
    private val relLinkedToHarm: String?,
    private val relCloseFamily: String?,
    private val experienceOfChildhood: String?,
    private val prevCloseRelationships: String?,
    private val relParentalResponsibilities: String?,
  ) : ScoredSection {
    override val section = NeedsSection.RELATIONSHIPS
    override val linkedToReOffending = YesNo.of(relLinkedToReoffending)
    override val linkedToHarm = YesNo.of(relLinkedToHarm)
    override val questionAnswers: Map<String, ScoredAnswer> = listOf(
      ::relCloseFamily,
      ::experienceOfChildhood,
      ::prevCloseRelationships,
    ).associate { it.name to ScoredAnswer.Problem.of(it.get()) }
    override val tierThreshold = TierThreshold(2, 5)
    override val oasysThreshold = OasysThreshold(2)

    val parentalResponsibilities = YesNo.of(relParentalResponsibilities)
  }

  data class LifestyleAndAssociates(
    private val lifestyleLinkedToReoffending: String?,
    private val lifestyleLinkedToHarm: String?,
    private val regActivitiesEncourageOffending: String?,
    private val easilyInfluenced: String?,
    private val recklessness: String?,
  ) : ScoredSection {
    override val section = NeedsSection.LIFESTYLE_AND_ASSOCIATES
    override val linkedToReOffending = YesNo.of(lifestyleLinkedToReoffending)
    override val linkedToHarm = YesNo.of(lifestyleLinkedToHarm)
    override val questionAnswers: Map<String, ScoredAnswer> = listOf(
      ::regActivitiesEncourageOffending,
      ::easilyInfluenced,
      ::recklessness,
    ).associate { it.name to ScoredAnswer.Problem.of(it.get()) }
    override val tierThreshold = TierThreshold(2, 5)
    override val oasysThreshold = OasysThreshold(2)
  }

  data class DrugMisuse(
    private val drugLinkedToReoffending: String?,
    private val drugLinkedToHarm: String?,
    private val currentDrugNoted: String?,
    @JsonAlias("LevelOfUseOfMainDrug") private val levelOfUseOfMainDrug: String?,
    private val everInjectedDrugs: String?,
    private val motivationToTackleDrugMisuse: String?,
    @JsonAlias("DrugsMajorActivity") private val drugsMajorActivity: String?,
  ) : ScoredSection {
    override val section = NeedsSection.DRUG_MISUSE
    override val linkedToReOffending = YesNo.of(drugLinkedToReoffending)
    override val linkedToHarm = YesNo.of(drugLinkedToHarm)
    override val questionAnswers: Map<String, ScoredAnswer> = buildList {
      addAll(
        listOf(::currentDrugNoted, ::levelOfUseOfMainDrug, ::motivationToTackleDrugMisuse, ::drugsMajorActivity)
          .map { it.name to ScoredAnswer.Problem.of(it.get()) },
      )
      add(::everInjectedDrugs.name to ScoredAnswer.Frequency.of(everInjectedDrugs))
    }.toMap()
    override val tierThreshold = TierThreshold(2, 8)
    override val oasysThreshold = OasysThreshold(2)
  }

  data class AlcoholMisuse(
    private val alcoholLinkedToReoffending: String?,
    private val alcoholLinkedToHarm: String?,
    private val currentUse: String?,
    private val bingeDrinking: String?,
    private val frequencyAndLevel: String?,
    private val alcoholTackleMotivation: String?,
  ) : ScoredSection {
    override val section = NeedsSection.ALCOHOL_MISUSE
    override val linkedToReOffending = YesNo.of(alcoholLinkedToReoffending)
    override val linkedToHarm = YesNo.of(alcoholLinkedToHarm)
    override val questionAnswers: Map<String, ScoredAnswer> = listOf(
      ::currentUse,
      ::bingeDrinking,
      ::frequencyAndLevel,
      ::alcoholTackleMotivation,
    ).associate { it.name to ScoredAnswer.Problem.of(it.get()) }
    override val tierThreshold = TierThreshold(4, 7)
    override val oasysThreshold = OasysThreshold(4)
  }

  data class ThinkingAndBehaviour(
    private val thinkLinkedToReoffending: String?,
    private val thinkLinkedToHarm: String?,
    private val recogniseProblems: String?,
    private val problemSolvingSkills: String?,
    private val awarenessOfConsequences: String?,
    private val understandsViewsOfOthers: String?,
    @JsonAlias("impulsivity") private val impulsivityStr: String?,
    @JsonAlias("temperControl") private val temperControlStr: String?,
  ) : ScoredSection {
    override val section = NeedsSection.THINKING_AND_BEHAVIOUR
    override val linkedToReOffending = YesNo.of(thinkLinkedToReoffending)
    override val linkedToHarm = YesNo.of(thinkLinkedToHarm)
    override val questionAnswers: Map<String, ScoredAnswer> = listOf(
      ::recogniseProblems,
      ::problemSolvingSkills,
      ::awarenessOfConsequences,
      ::understandsViewsOfOthers,
    ).associate { it.name to ScoredAnswer.Problem.of(it.get()) }
    override val tierThreshold = TierThreshold(4, 7)
    override val oasysThreshold = OasysThreshold(4)

    val impulsivity = ScoredAnswer.Problem.of(impulsivityStr)
    val temperControl = ScoredAnswer.Problem.of(temperControlStr)
  }

  data class Attitudes(
    private val attLinkedToReoffending: String?,
    private val attLinkedToHarm: String?,
    private val proCriminalAttitudes: String?,
    private val attitudesTowardsSupervision: String?,
    private val attitudesTowardsCommunitySociety: String?,
    private val motivationToAddressBehaviour: String?,
  ) : ScoredSection {
    override val section = NeedsSection.ATTITUDE
    override val linkedToReOffending = YesNo.of(attLinkedToReoffending)
    override val linkedToHarm = YesNo.of(attLinkedToHarm)
    override val questionAnswers: Map<String, ScoredAnswer> = listOf(
      ::proCriminalAttitudes,
      ::attitudesTowardsSupervision,
      ::attitudesTowardsCommunitySociety,
      ::motivationToAddressBehaviour,
    ).associate { it.name to ScoredAnswer.Problem.of(it.get()) }
    override val tierThreshold = TierThreshold(2, 7)
    override val oasysThreshold = OasysThreshold(2)
  }
}

sealed interface ScoredAnswer {
  val score: Int

  enum class YesNo(override val score: Int) : ScoredAnswer {
    Yes(2),
    No(0),
    Unknown(0),
    ;

    companion object {
      fun of(value: String?): YesNo = entries.firstOrNull { it.name.equals(value, true) } ?: Unknown
    }
  }

  enum class Problem(override val score: Int) : ScoredAnswer {
    None(0),
    Some(1),
    Significant(2),
    Missing(0),
    ;

    companion object {
      fun of(value: String?): Problem = when (value?.firstOrNull()) {
        '0' -> None
        '1' -> Some
        '2' -> Significant
        else -> Missing
      }
    }
  }

  enum class Frequency(override val score: Int) : ScoredAnswer {
    Never(0),
    Previous(1),
    Currently(2),
    Unknown(0),
    ;

    companion object {
      fun of(value: String?): Frequency = entries.firstOrNull { it.name.equals(value, true) } ?: Unknown
    }
  }
}
