package uk.gov.justice.digital.hmpps.assessrisksandneeds.testutils

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.NeedsSection

class OasysApiMockServer : WireMockServer(9097) {
  private val crn = "X123456"
  private val badCrn = "X999999"
  private val missingRsrCrn = "X234567"

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
            .withBody(
              this::class.java.getResource("/json/ordsAssessmentOffenceNoCompleteAssessments.json")?.readText(),
            ),
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

  fun stubGetAssessmentTimeline() {
    stubFor(
      WireMock.get(
        WireMock.urlEqualTo(
          "/eor/oasys/ass/allasslist/prob/X123456/ALLOW",
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
          "/eor/oasys/ass/allasslist/pris/A1234YZ/ALLOW",
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
          "/eor/oasys/ass/allasslist/prob/$missingRsrCrn/ALLOW",
        ),
      )
        .willReturn(
          WireMock.aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withStatus(200)
            .withBody(this::class.java.getResource("/json/ordsAssessmentTimelineNullRoshScores.json")?.readText()),
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

  fun stubGetTierSections() {
    NeedsSection.entries.forEach {
      stubFor(
        WireMock.get(
          WireMock.urlEqualTo(
            "/eor/oasys/ass/section${it.sectionNumber}/ALLOW/9630348",
          ),
        ).willReturn(
          WireMock.aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withStatus(200)
            .withBody(this::class.java.getResource("/json/ordsTierSection${it.sectionNumber}.json")?.readText()),
        ),
      )
    }
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

    stubFor(
      WireMock.get(WireMock.urlEqualTo("/eor/oasys/ass/allrisk/$missingRsrCrn/ALLOW"))
        .willReturn(
          WireMock.aResponse()
            .withBody(noRsrPredictorsJson)
            .withStatus(200)
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json"))),
        ),
    )
  }

  fun stubGetRoshRisksByCrn() {
    stubFor(
      WireMock.get(
        WireMock.urlPathMatching(
          "/eor/oasys/ass/sectionroshfull/ALLOW/9630348",
        ),
      ).willReturn(
        WireMock.aResponse()
          .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
          .withBody(this::class.java.getResource("/json/ordsAssessmentRoshFull.json")?.readText()),
      ),
    )

    stubFor(
      WireMock.get(
        WireMock.urlPathMatching(
          "/eor/oasys/ass/sectionrosh/ALLOW/9630348",
        ),
      ).willReturn(
        WireMock.aResponse()
          .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
          .withBody(this::class.java.getResource("/json/ordsAssessmentRoshScreening.json")?.readText()),
      ),
    )

    stubFor(
      WireMock.get(
        WireMock.urlPathMatching(
          "/eor/oasys/ass/sectionroshsumm/ALLOW/9630348",
        ),
      ).willReturn(
        WireMock.aResponse()
          .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
          .withBody(this::class.java.getResource("/json/ordsAssessmentRoshSummary.json")?.readText()),
      ),
    )

    stubFor(
      WireMock.get(
        WireMock.urlPathMatching(
          "/eor/oasys/ass/sectionroshsumm/ALLOW/45115261",
        ),
      ).willReturn(
        WireMock.aResponse()
          .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
          .withBody(this::class.java.getResource("/json/ordsAssessmentRoshSummaryForNull.json")?.readText()),
      ),
    )

    stubFor(
      WireMock.get(
        WireMock.urlPathMatching(
          "/eor/oasys/ass/sectionroshsumm/ALLOW/45115261",
        ),
      ).willReturn(
        WireMock.aResponse()
          .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
          .withBody(this::class.java.getResource("/json/ordsAssessmentRoshSummaryForNull.json")?.readText()),
      ),
    )
  }

  companion object {
    val crnNotFoundJson =
      """{ "status": 404 , "developerMessage": "Latest COMPLETE with types [LAYER_1, LAYER_3] type not found for crn, RANDOMCRN" }""".trimIndent()
  }

  private val noRsrPredictorsJson = """
  {  
    "assessments": [
      {
          "dateCompleted": "2021-06-21T15:55:04",
          "assessmentStatus": "COMPLETE",
          "OGRS": {},
          "OVP": {},
          "OGP": {},
          "RSR": {},
          "OSP": {}
      }
    ]
  } 
  """.trimIndent()
}
