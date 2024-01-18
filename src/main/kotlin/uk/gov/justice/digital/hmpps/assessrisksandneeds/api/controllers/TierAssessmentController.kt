package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.controllers

import io.swagger.v3.oas.annotations.Operation
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PersonIdentifier
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.OasysApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.oasys.section.ScoredSection
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.NeedsSection

@RestController
@RequestMapping("tier-assessment")
class TierAssessmentController(private val ordsApiClient: OasysApiRestClient) {
  @GetMapping("/sections/{crn}")
  @Operation(description = "Gets all question answers required for a tier calculation")
  @PreAuthorize("hasRole('ROLE_MANAGEMENT_TIER_UPDATE')")
  fun getTierAssessmentAnswers(
    @PathVariable crn: String,
  ) = ordsApiClient.getScoredSections(PersonIdentifier(PersonIdentifier.Type.CRN, crn), NeedsSection.entries).let {
    TierAnswers(
      it.section(NeedsSection.ACCOMMODATION),
      it.section(NeedsSection.EDUCATION_TRAINING_EMPLOYMENT),
      it.section(NeedsSection.RELATIONSHIPS),
      it.section(NeedsSection.LIFESTYLE),
      it.section(NeedsSection.DRUG_MISUSE),
      it.section(NeedsSection.ALCOHOL_MISUSE),
      it.section(NeedsSection.THINKING_AND_BEHAVIOUR),
      it.section(NeedsSection.ATTITUDE),
    )
  }
}

inline fun <reified T : ScoredSection> Map<NeedsSection, ScoredSection>.section(section: NeedsSection): T? =
  this[section] as T?

data class TierAnswers(
  val accommodation: ScoredSection.Accommodation?,
  val educationTrainingEmployment: ScoredSection.EducationTrainingEmployment?,
  val relationships: ScoredSection.Relationships?,
  val lifestyleAndAssociates: ScoredSection.LifestyleAndAssociates?,
  val drugMisuse: ScoredSection.DrugMisuse?,
  val alcoholMisuse: ScoredSection.AlcoholMisuse?,
  val thinkingAndBehaviour: ScoredSection.ThinkingAndBehaviour?,
  val attitudes: ScoredSection.Attitudes?,
)
