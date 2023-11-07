package uk.gov.justice.digital.hmpps.assessrisksandneeds.testutils

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders

class OasysApiMockServer : WireMockServer(9097) {
  private val crn = "X123456"
  private val badCrn = "X999999"

  fun stubGetAssessmentOffenceByCrn() {
    stubFor(
      WireMock.get(
        WireMock.urlEqualTo(
          "/eor/oasys/ass/offence/$crn/ALLOW",
        ),
      )
        .willReturn(
          WireMock.aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody(this::class.java.getResource("/json/ordsAssessmentOffence.json")?.readText()),
        ),
    )

    stubFor(
      WireMock.get(
        WireMock.urlEqualTo(
          "/eor/oasys/ass/offence/X654321/ALLOW",
        ),
      )
        .willReturn(
          WireMock.aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody(this::class.java.getResource("/json/ordsAssessmentOffenceNoCompleteAssessments.json")?.readText()),
        ),
    )

    stubFor(
      WireMock.get(
        WireMock.urlEqualTo(
          "/eor/oasys/ass/offence/NOT_FOUND/ALLOW",
        ),
      )
        .willReturn(
          WireMock.aResponse()
            .withBody("{}")
            .withStatus(404)
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json"))),
        ),
    )
  }

  fun stubGetAssessmentTimelineByCrn() {
    stubFor(
      WireMock.get(
        WireMock.urlEqualTo(
          "/eor/oasys/ass/timeline/X123456/ALLOW",
        ),
      )
        .willReturn(
          WireMock.aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withStatus(200)
            .withBody(this::class.java.getResource("/json/ordsAssessmentTimeline.json")?.readText()),
        ),
    )

    stubFor(
      WireMock.get(
        WireMock.urlEqualTo(
          "/eor/oasys/ass/timeline/crn/NOT_FOUND/ALLOW",
        ),
      )
        .willReturn(
          WireMock.aResponse()
            .withBody("{}")
            .withStatus(404)
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json"))),
        ),
    )
  }

  fun stubGetRiskManagementPlansByCrn() {
    stubFor(
      WireMock.get(
        WireMock.urlEqualTo(
          "/eor/oasys/ass/rmp/$crn/ALLOW",
        ),
      )
        .willReturn(
          WireMock.aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody(this::class.java.getResource("/json/ordsRiskManagementPlan.json")?.readText()),
        ),
    )

    stubFor(
      WireMock.get(
        WireMock.urlEqualTo(
          "/eor/oasys/ass/rmp/X654321/ALLOW",
        ),
      )
        .willReturn(
          WireMock.aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody(this::class.java.getResource("/json/ordsRiskManagementPlan.json")?.readText()),
        ),
    )

    stubFor(
      WireMock.get(
        WireMock.urlEqualTo(
          "/eor/oasys/ass/rmp/NOT_FOUND/LIMIT",
        ),
      )
        .willReturn(
          WireMock.aResponse()
            .withBody("{}")
            .withStatus(404)
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json"))),
        ),
    )
  }
  fun stubGetRiskPredictorScores() {
    stubFor(
      WireMock.get(
        WireMock.urlEqualTo(
          "/eor/oasys/ass/allrisk/$crn/ALLOW",
        ),
      )
        .willReturn(
          WireMock.aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody(this::class.java.getResource("/json/ordsRiskPredictors.json")?.readText()),
        ),
    )
    stubFor(
      WireMock.get(WireMock.urlEqualTo("/eor/oasys/ass/allrisk/$badCrn/ALLOW"))
        .willReturn(
          WireMock.aResponse()
            .withBody(crnNotFoundJson)
            .withStatus(404)
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json"))),
        ),
    )
  }

  companion object {
    val crnNotFoundJson =
      """{ "status": 404 , "developerMessage": "Latest COMPLETE with types [LAYER_1, LAYER_3] type not found for crn, RANDOMCRN" }""".trimIndent()
  }
}