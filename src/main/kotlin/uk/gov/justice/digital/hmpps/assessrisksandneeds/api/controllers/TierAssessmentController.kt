package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.controllers

import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PersonIdentifier
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.OasysApiRestClient
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.TierAnswers
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.NeedsSection

@RestController
@RequestMapping("tier-assessment")
class TierAssessmentController(private val ordsApiClient: OasysApiRestClient) {
  @GetMapping("/sections/{crn}")
  @Operation(description = "Gets all question answers required for a tier calculation")
  @PreAuthorize("hasRole('ROLE_MANAGEMENT_TIER_UPDATE')")
  fun getTierAssessmentAnswers(
    @PathVariable crn: String,
  ): ResponseEntity<TierAnswers> {
    val answers =
      ordsApiClient.getScoredSections(PersonIdentifier(PersonIdentifier.Type.CRN, crn), NeedsSection.entries)
    return if (answers == null) {
      ResponseEntity.notFound().build()
    } else {
      ResponseEntity.ok(answers)
    }
  }
}
