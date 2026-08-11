package uk.gov.justice.digital.hmpps.assessrisksandneeds.e2e

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AssessmentNeedsDetailsDto

@DisplayName("ARNS API Tests")
class ArnsApiTest : IntegrationTestBase() {

  @Test
  fun `get assessment needs by crn`() {
    val needsResponse = webTestClient.get().uri("/needs/$crn")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<AssessmentNeedsDetailsDto>()
      .returnResult().responseBody

    assertThat(needsResponse?.needs).isNotEmpty()
  }
}
