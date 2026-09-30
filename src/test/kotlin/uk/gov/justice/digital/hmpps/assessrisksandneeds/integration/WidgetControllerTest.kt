package uk.gov.justice.digital.hmpps.assessrisksandneeds.integration

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.test.web.reactive.server.expectBody

@AutoConfigureWebTestClient
@DisplayName("Risk widget Tests")
class WidgetControllerTest : IntegrationTestBase() {

  private val crn = "X123456"

  @Test
  fun `get risk summary by crn for external provider`() {
    webTestClient.get().uri("/risks/crn/$crn/widget")
      .headers(setAuthorisation(roles = listOf("ROLE_CRS_PROVIDER")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .consumeWith {
        JSONAssert.assertEquals(
          """{"overallRisk":"VERY_HIGH","riskInCommunity":{"Children":"LOW","Public":"MEDIUM","Known Adult":"LOW","Staff":"HIGH"}}""",
          checkNotNull(it.responseBody),
          JSONCompareMode.STRICT,
        )
      }
  }

  @Test
  fun `get risk summary by crn for external provider within timeframe`() {
    val timeframe = 60L
    webTestClient.get().uri("/risks/crn/$crn/widget/$timeframe")
      .headers(setAuthorisation(roles = listOf("ROLE_CRS_PROVIDER")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .consumeWith {
        JSONAssert.assertEquals(
          """{"overallRisk":"VERY_HIGH","riskInCommunity":{"Children":"LOW","Public":"MEDIUM","Known Adult":"LOW","Staff":"HIGH"}}""",
          checkNotNull(it.responseBody),
          JSONCompareMode.STRICT,
        )
      }
  }

  @Test
  fun `get risk summary by crn for external provider not within timeframe`() {
    val timeframe = 2L
    webTestClient.get().uri("/risks/crn/$crn/widget/$timeframe")
      .headers(setAuthorisation(roles = listOf("ROLE_CRS_PROVIDER")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .consumeWith {
        JSONAssert.assertEquals(
          """{"riskInCommunity":{}}""",
          checkNotNull(it.responseBody),
          JSONCompareMode.STRICT,
        )
      }
  }

  @Test
  fun `get risk summary by crn for external provider within timeframe - no risk summary`() {
    val timeframe = 5L
    webTestClient.get().uri("/risks/crn/$crn/widget/$timeframe")
      .headers(setAuthorisation(roles = listOf("ROLE_CRS_PROVIDER")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .consumeWith {
        JSONAssert.assertEquals(
          """{"riskInCommunity":{}}""",
          checkNotNull(it.responseBody),
          JSONCompareMode.STRICT,
        )
      }
  }

  @Test
  fun `get risk summary by crn with timeframe query param matches the deprecated path variant`() {
    val timeframe = 60L
    val fromQueryParam = webTestClient.get().uri("/risks/crn/$crn/widget?timeframe=$timeframe")
      .headers(setAuthorisation(roles = listOf("ROLE_CRS_PROVIDER")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody

    val fromPath = webTestClient.get().uri("/risks/crn/$crn/widget/$timeframe")
      .headers(setAuthorisation(roles = listOf("ROLE_CRS_PROVIDER")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody

    JSONAssert.assertEquals(
      checkNotNull(fromQueryParam),
      checkNotNull(fromPath),
      JSONCompareMode.STRICT,
    )
  }

  @Test
  fun `get risk summary by crn with a timeframe query param that excludes all assessments`() {
    val timeframe = 2L
    webTestClient.get().uri("/risks/crn/$crn/widget?timeframe=$timeframe")
      .headers(setAuthorisation(roles = listOf("ROLE_CRS_PROVIDER")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .consumeWith {
        JSONAssert.assertEquals(
          """{"riskInCommunity":{}}""",
          checkNotNull(it.responseBody),
          JSONCompareMode.STRICT,
        )
      }
  }

  @Test
  fun `get risk summary by crn for probation practitioner`() {
    webTestClient.get().uri("/risks/crn/$crn/widget")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .consumeWith {
        JSONAssert.assertEquals(
          """
            {
              "overallRisk": "VERY_HIGH",
              "assessedOn": "2024-12-19T16:57:25",
              "riskInCommunity": {
                "Children": "LOW",
                "Public": "MEDIUM",
                "Known Adult": "LOW",
                "Staff": "HIGH"
              },
              "riskInCustody": {
                "Children": "LOW",
                "Public": "LOW",
                "Known Adult": "LOW",
                "Staff": "VERY_HIGH",
                "Prisoners": "HIGH"
              }
            }
          """.trimIndent(),
          checkNotNull(it.responseBody),
          JSONCompareMode.STRICT,
        )
      }
  }
}
