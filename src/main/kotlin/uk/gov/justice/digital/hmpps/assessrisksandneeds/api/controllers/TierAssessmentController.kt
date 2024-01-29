package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.controllers

import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.PersonIdentifier
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.SectionSummary
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.NeedsSection
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.TierService

@RestController
@RequestMapping("tier-assessment")
class TierAssessmentController(private val tierService: TierService) {
  @GetMapping("/sections/{crn}")
  @Operation(description = "Gets all question answers required for a tier calculation")
  @PreAuthorize("hasRole('ROLE_MANAGEMENT_TIER_UPDATE')")
  fun getTierAssessmentAnswers(
    @PathVariable crn: String,
  ): ResponseEntity<SectionSummary> {
    val answers = tierService.getSectionsForTier(PersonIdentifier(PersonIdentifier.Type.CRN, crn), NeedsSection.entries)
    return if (answers == null) {
      ResponseEntity.notFound().build()
    } else {
      ResponseEntity.ok(answers)
    }
  }
}
