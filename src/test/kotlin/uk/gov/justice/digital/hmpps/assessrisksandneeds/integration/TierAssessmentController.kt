package uk.gov.justice.digital.hmpps.assessrisksandneeds.integration

import com.fasterxml.jackson.databind.JsonNode
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.test.web.reactive.server.expectBody

@AutoConfigureWebTestClient
@DisplayName("Tier calculation information tests")
class TierAssessmentController : IntegrationTestBase() {
  @Test
  fun `successfully returns the answers required for a tier calculation`() {
    val response = checkNotNull(
      webTestClient.get()
        .uri("/tier-assessment/sections/X123456")
        .headers(setAuthorisation(roles = listOf("ROLE_MANAGEMENT_TIER_UPDATE")))
        .exchange()
        .expectStatus().isOk
        .expectBody<JsonNode>()
        .returnResult().responseBody,
    )

    val assessmentSummary = checkNotNull(response["assessment"])
    assertThat(assessmentSummary["assessmentId"].asInt(), equalTo(9630348))
    assertThat(assessmentSummary["completedDate"].asText(), equalTo("2023-12-19T16:57:25"))

    val accommodation = checkNotNull(response["accommodation"])
    assertThat(accommodation.severity, equalTo("NO_NEED"))

    val ete = checkNotNull(response["educationTrainingEmployability"])
    assertThat(ete.severity, equalTo("STANDARD"))

    val relationships = checkNotNull(response["relationships"])
    assertThat(relationships.severity, equalTo("STANDARD"))
    assertThat(relationships["parentalResponsibilities"]?.asText(), equalTo("No"))

    val lifestyle = checkNotNull(response["lifestyleAndAssociates"])
    assertThat(lifestyle.severity, equalTo("STANDARD"))

    val drugMisuse = checkNotNull(response["drugMisuse"])
    assertThat(drugMisuse.severity, equalTo("NO_NEED"))

    val alcoholMisuse = checkNotNull(response["alcoholMisuse"])
    assertThat(alcoholMisuse.severity, equalTo("STANDARD"))

    val thinkingAndBehaviour = checkNotNull(response["thinkingAndBehaviour"])
    assertThat(thinkingAndBehaviour.severity, equalTo("SEVERE"))
    assertThat(thinkingAndBehaviour["impulsivity"]?.asText(), equalTo("Significant"))
    assertThat(thinkingAndBehaviour["temperControl"]?.asText(), equalTo("Some"))

    val attitudes = checkNotNull(response["attitudes"])
    assertThat(attitudes.severity, equalTo("NO_NEED"))
  }

  private val JsonNode.severity get(): String = this["severity"].asText()
}
