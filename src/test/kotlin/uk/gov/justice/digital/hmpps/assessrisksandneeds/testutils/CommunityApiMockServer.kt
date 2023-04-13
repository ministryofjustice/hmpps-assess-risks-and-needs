package uk.gov.justice.digital.hmpps.assessrisksandneeds.testutils

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders

class CommunityApiMockServer : WireMockServer(9096) {

  fun stubGetUserAccess() {
    stubFor(
      WireMock.get(
        WireMock.urlPathMatching(
          "/secure/offenders/crn/(?:X123456|NOT_FOUND|X654321|X999999)/user/assess-risks-needs/userAccess",
        ),
      )
        .willReturn(
          WireMock.aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody(laoSuccess),
        ),
    )

    stubFor(
      WireMock.get(
        WireMock.urlEqualTo(
          "/secure/offenders/crn/FORBIDDEN/user/assess-risks-needs/userAccess",
        ),
      )
        .willReturn(
          WireMock.aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withStatus(403)
            .withBody(laoFailure),
        ),
    )

    stubFor(
      WireMock.get(
        WireMock.urlEqualTo(
          "/secure/offenders/crn/USER_ACCESS_NOT_FOUND/user/assess-risks-needs/userAccess",
        ),
      )
        .willReturn(
          WireMock.aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withStatus(404),
        ),
    )

    stubFor(
      WireMock.get(
        WireMock.urlEqualTo(
          "/secure/offenders/crn/X123456/user/USER_NOT_FOUND/userAccess",
        ),
      )
        .willReturn(
          WireMock.aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withStatus(404)
            .withBody("Can't resolve user: USER_NOT_FOUND"),
        ),
    )
  }

  private val laoSuccess = """{
      "exclusionMessage": null,
      "restrictionMessage": null,
      "userExcluded": false,
      "userRestricted": false
    }
  """.trimIndent()

  private val laoFailure = """{
      "exclusionMessage": "excluded",
      "restrictionMessage": "restricted",
      "userExcluded": true,
      "userRestricted": true
    }
  """.trimIndent()
}
