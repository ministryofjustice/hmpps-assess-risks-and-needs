package uk.gov.justice.digital.hmpps.assessrisksandneeds.e2e

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.MappsAssessmentTimeline

@DisplayName("MAPPS API Tests")
class MappsArnsApiTest : IntegrationTestBase() {

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
