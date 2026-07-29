package uk.gov.justice.digital.hmpps.assessrisksandneeds.e2e

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.MappsAssessmentTimeline
import uk.gov.justice.digital.hmpps.assessrisksandneeds.e2e.dto.TokenDto

@DisplayName("MAPPS API Tests")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MappsArnsApiTest {
  protected lateinit var webTestClient: WebTestClient

  protected lateinit var authTestClient: WebTestClient

  private val crn = System.getenv("ARNS_API_CRN") ?: "X643390"

  @BeforeAll
  fun setup() {
    authTestClient = WebTestClient.bindToServer()
      .baseUrl("https://sign-in-dev.hmpps.service.justice.gov.uk")
      .build()

    val clientId = System.getenv("AAP_CLIENT_ID") ?: "local-development-client-id"
    val clientSecret = System.getenv("AAP_CLIENT_SECRET") ?: "default_secret"

    val body = authTestClient.post()
      .uri("/auth/oauth/token")
      .headers { it.setBasicAuth(clientId, clientSecret) }
      .contentType(MediaType.APPLICATION_FORM_URLENCODED)
      .body(BodyInserters.fromFormData("grant_type", "client_credentials"))
      .exchange()
      .expectStatus()
      .isOk()
      .expectBody(TokenDto::class.java)
      .returnResult()
      .responseBody

    val token = body?.access_token

    webTestClient = WebTestClient.bindToServer()
      .baseUrl(System.getenv("BASE_URL") ?: "https://assess-risks-and-needs-dev.hmpps.service.justice.gov.uk")
      .defaultHeader("Authorization", "Bearer $token")
      .build()
  }

  @Test
  fun `get Mapps assessment timeline by crn`() {
    val timelineResponse = webTestClient.get().uri("/assessments/mapps/crn/$crn")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<MappsAssessmentTimeline>()
      .returnResult().responseBody

    assertThat(timelineResponse?.assessments).isNotEmpty()
  }
}
