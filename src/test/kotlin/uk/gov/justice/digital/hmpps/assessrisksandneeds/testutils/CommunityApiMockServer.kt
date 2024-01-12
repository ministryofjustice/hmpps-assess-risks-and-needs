package uk.gov.justice.digital.hmpps.assessrisksandneeds.testutils

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders

class CommunityApiMockServer : WireMockServer(9096) {

  fun stubGetUserAccess() {
    stubFor(
      WireMock.post(
        WireMock.urlPathEqualTo("/users/access"),
      )
        .withQueryParam("username", equalTo("assess-risks-needs"))
        .willReturn(
          WireMock.aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody(laoResponse),
        ),
    )
  }

  private val laoResponse = """
    {
      "access": [
        {
          "userExcluded": false,
          "userRestricted": false,
          "crn": "X123456"
        },
         {
          "userExcluded": false,
          "userRestricted": false,
          "crn": "X12345"
        },
        {
          "userExcluded": false,
          "userRestricted": false,
          "crn": "X234567"
        },
        {
          "userExcluded": false,
          "userRestricted": false,
          "crn": "X654321"
        },        
        {
          "userExcluded": true,
          "userRestricted": false,
          "excludedMessage": "excluded",
          "crn": "FORBIDDEN"
        }
      ]
    }
  """.trimIndent()
}
