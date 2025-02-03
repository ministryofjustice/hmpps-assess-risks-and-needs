package uk.gov.justice.digital.hmpps.assessrisksandneeds.integration

import com.fasterxml.jackson.databind.JsonNode
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.time.LocalDateTime

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
    assertThat(assessmentSummary["completedDate"].asText(), equalTo("${LocalDateTime.now().year - 1}-12-19T16:57:25"))
    assertThat(assessmentSummary["status"].asText(), equalTo("COMPLETE"))

    val accommodation = checkNotNull(response["accommodation"])
    assertThat(accommodation.severity, equalTo("NO_NEED"))
    assertThat(accommodation.linkedToReOffending, equalTo(NO))
    assertThat(accommodation.linkedToHarm, equalTo(NO))
    assertThat(accommodation.score, equalTo(0))

    val ete = checkNotNull(response["educationTrainingEmployability"])
    assertThat(ete.severity, equalTo("STANDARD"))
    assertThat(ete.linkedToReOffending, equalTo(NO))
    assertThat(ete.linkedToHarm, equalTo(NO))
    assertThat(ete.score, equalTo(3))

    val relationships = checkNotNull(response["relationships"])
    assertThat(relationships.severity, equalTo("STANDARD"))
    assertThat(relationships.linkedToReOffending, equalTo(NO))
    assertThat(relationships.linkedToHarm, equalTo(NO))
    assertThat(relationships.score, equalTo(3))
    assertThat(relationships["parentalResponsibilities"]?.asText(), equalTo("No"))

    val lifestyle = checkNotNull(response["lifestyleAndAssociates"])
    assertThat(lifestyle.severity, equalTo("STANDARD"))
    assertThat(lifestyle.linkedToReOffending, equalTo(YES))
    assertThat(lifestyle.linkedToHarm, equalTo(YES))
    assertThat(lifestyle.score, equalTo(3))

    val drugMisuse = checkNotNull(response["drugMisuse"])
    assertThat(drugMisuse.severity, equalTo("NO_NEED"))
    assertThat(drugMisuse.linkedToReOffending, equalTo(UNKNOWN))
    assertThat(drugMisuse.linkedToHarm, equalTo(UNKNOWN))
    assertThat(drugMisuse.score, equalTo(0))

    val alcoholMisuse = checkNotNull(response["alcoholMisuse"])
    assertThat(alcoholMisuse.severity, equalTo("STANDARD"))
    assertThat(alcoholMisuse.linkedToReOffending, equalTo(YES))
    assertThat(alcoholMisuse.linkedToHarm, equalTo(NO))
    assertThat(alcoholMisuse.score, equalTo(4))

    val thinkingAndBehaviour = checkNotNull(response["thinkingAndBehaviour"])
    assertThat(thinkingAndBehaviour.severity, equalTo("SEVERE"))
    assertThat(thinkingAndBehaviour.linkedToReOffending, equalTo(YES))
    assertThat(thinkingAndBehaviour.linkedToHarm, equalTo(YES))
    assertThat(thinkingAndBehaviour.score, equalTo(7))
    assertThat(thinkingAndBehaviour["impulsivity"]?.asText(), equalTo("Significant"))
    assertThat(thinkingAndBehaviour["temperControl"]?.asText(), equalTo("Some"))

    val attitudes = checkNotNull(response["attitudes"])
    assertThat(attitudes.severity, equalTo("NO_NEED"))
    assertThat(attitudes.linkedToReOffending, equalTo(NO))
    assertThat(attitudes.linkedToHarm, equalTo(NO))
    assertThat(attitudes.score, equalTo(0))
  }

  private val JsonNode.severity get(): String = this["severity"].asText()
  private val JsonNode.linkedToReOffending get(): String = this["linkedToReOffending"].asText()
  private val JsonNode.linkedToHarm get(): String = this["linkedToHarm"].asText()
  private val JsonNode.score get(): Int = this["score"].asInt()

  companion object {
    const val YES = "Yes"
    const val NO = "No"
    const val UNKNOWN = "Unknown"
  }
}
