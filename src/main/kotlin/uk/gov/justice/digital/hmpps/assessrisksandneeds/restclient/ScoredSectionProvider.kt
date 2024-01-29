package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient

import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.oasys.section.ScoredSection
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.oasys.section.ScoredSectionResponse
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.NeedsSection

object ScoredSectionProvider {
  fun mapSection(needsSection: NeedsSection): (res: ClientResponse) -> Mono<Pair<NeedsSection, ScoredSection>> =
    { res ->
      if (res.statusCode() == HttpStatus.OK) {
        map(needsSection)(res)
      } else {
        Mono.empty()
      }
    }

  private fun map(needsSection: NeedsSection): (res: ClientResponse) -> Mono<Pair<NeedsSection, ScoredSection>> =
    when (needsSection) {
      NeedsSection.ACCOMMODATION -> {
        {
          it.bodyToMono<ScoredSectionResponse<ScoredSection.Accommodation>>().section(needsSection)
        }
      }

      NeedsSection.EDUCATION_TRAINING_AND_EMPLOYABILITY -> {
        {
          it.bodyToMono<ScoredSectionResponse<ScoredSection.EducationTrainingEmployability>>().section(needsSection)
        }
      }

      NeedsSection.RELATIONSHIPS -> {
        {
          it.bodyToMono<ScoredSectionResponse<ScoredSection.Relationships>>().section(needsSection)
        }
      }

      NeedsSection.LIFESTYLE_AND_ASSOCIATES -> {
        {
          it.bodyToMono<ScoredSectionResponse<ScoredSection.LifestyleAndAssociates>>().section(needsSection)
        }
      }

      NeedsSection.DRUG_MISUSE -> {
        {
          it.bodyToMono<ScoredSectionResponse<ScoredSection.DrugMisuse>>().section(needsSection)
        }
      }

      NeedsSection.ALCOHOL_MISUSE -> {
        {
          it.bodyToMono<ScoredSectionResponse<ScoredSection.AlcoholMisuse>>().section(needsSection)
        }
      }

      NeedsSection.THINKING_AND_BEHAVIOUR -> {
        {
          it.bodyToMono<ScoredSectionResponse<ScoredSection.ThinkingAndBehaviour>>().section(needsSection)
        }
      }

      NeedsSection.ATTITUDE -> {
        {
          it.bodyToMono<ScoredSectionResponse<ScoredSection.Attitudes>>().section(needsSection)
        }
      }
    }

  private fun <T : ScoredSection> Mono<ScoredSectionResponse<T>>.section(needsSection: NeedsSection): Mono<Pair<NeedsSection, ScoredSection>> =
    mapNotNull { res -> res.section?.let { needsSection to it } }
}
