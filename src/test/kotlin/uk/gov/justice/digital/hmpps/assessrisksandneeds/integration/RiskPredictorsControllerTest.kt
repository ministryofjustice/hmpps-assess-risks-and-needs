package uk.gov.justice.digital.hmpps.assessrisksandneeds.integration

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.AuditService

@AutoConfigureWebTestClient(timeout = "360000000")
@DisplayName("Risk Predictors Tests")
class RiskPredictorsControllerTest : IntegrationTestBase() {

  @MockkBean
  private lateinit var auditService: AuditService

  @BeforeEach
  fun setup() {
    every { auditService.sendEvent(any(), any()) } returns Unit
  }

  @Test
  fun `get all rsr scores should convert identifier type regardless of case`() {
    val identifierType = "cRn"
    val identifierValue = "X234567"

    webTestClient.get().uri("/risks/predictors/rsr/$identifierType/$identifierValue")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isOk
  }

  @Test
  fun `get all rsr scores for a crn identifier type`() {
    val identifierType = "crn"
    val identifierValue = "X123456"

    webTestClient.get()
      .uri("/risks/predictors/rsr/$identifierType/$identifierValue")
      .header("Content-Type", "application/json")
      .headers(setAuthorisation(user = "assess-risks-needs", roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody<String>()
      .consumeWith {
        JSONAssert.assertEquals(rsrScoresJson, checkNotNull(it.responseBody), JSONCompareMode.STRICT)
      }
  }

  @Test
  fun `get all rsr scores for a crn identifier type when no rsr returned from assessment API`() {
    val identifierType = "CRN"
    val identifierValue = "X234567"

    webTestClient.get()
      .uri("/risks/predictors/rsr/$identifierType/$identifierValue")
      .header("Content-Type", "application/json")
      .headers(setAuthorisation(user = "assess-risks-needs", roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody<String>()
      .consumeWith {
        JSONAssert.assertEquals("[]", checkNotNull(it.responseBody), JSONCompareMode.STRICT)
      }
  }

  @Test
  fun `get all rsr scores should return bad request for invalid identifier type`() {
    val identifierType = "INVALID_IDENTIFIER_TYPE"
    val identifierValue = "X234567"

    webTestClient.get().uri("/risks/predictors/rsr/$identifierType/$identifierValue")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isBadRequest
  }

  private companion object {
    val rsrScoresJson = """
      [
        {
          "completedDate": "2026-07-27T15:40:41",
          "source": "OASYS",
          "status": "COMPLETE",
          "outputVersion": "2",
          "output": {
            "seriousViolentReoffendingPredictor": {
              "staticOrDynamic": "STATIC",
              "score": 1.79,
              "band": "MEDIUM"
            },
            "directContactSexualReoffendingPredictor": {
              "score": 0,
              "band": "NOT_APPLICABLE"
            },
            "indirectImageContactSexualReoffendingPredictor": {
              "score": 0,
              "band": "NOT_APPLICABLE"
            },
            "combinedSeriousReoffendingPredictor": {
              "algorithmVersion": "6",
              "staticOrDynamic": "STATIC",
              "score": 1.79,
              "band": "MEDIUM"
            }
          }
        },
        {
          "completedDate": "2022-06-12T18:23:20",
          "source": "OASYS",
          "status": "COMPLETE",
          "outputVersion": "2",
          "output": {
            "seriousViolentReoffendingPredictor": {
              "staticOrDynamic": "STATIC",
              "score": 1.23,
              "band": "LOW"
            },
            "directContactSexualReoffendingPredictor": {
              "score": 2.81,
              "band": "MEDIUM"
            },
            "indirectImageContactSexualReoffendingPredictor": {
              "score": 1.07,
              "band": "MEDIUM"
            },
            "combinedSeriousReoffendingPredictor": {
              "algorithmVersion": "6",
              "staticOrDynamic": "STATIC",
              "score": 1.23,
              "band": "LOW"
            }
          }
        },
        {
          "completedDate": "2022-06-11T18:23:20",
          "source": "OASYS",
          "status": "COMPLETE",
          "outputVersion": "2",
          "output": {
            "seriousViolentReoffendingPredictor": {
              "staticOrDynamic": "DYNAMIC",
              "score": 4.56,
              "band": "MEDIUM"
            },
            "directContactSexualReoffendingPredictor": {
              "score": 2.81,
              "band": "MEDIUM"
            },
            "indirectImageContactSexualReoffendingPredictor": {
              "score": 1.07,
              "band": "MEDIUM"
            },
            "combinedSeriousReoffendingPredictor": {
              "algorithmVersion": "6",
              "staticOrDynamic": "DYNAMIC",
              "score": 50.1234,
              "band": "MEDIUM"
            }
          }
        },
        {
          "completedDate": "2022-06-10T18:23:20",
          "source": "OASYS",
          "status": "COMPLETE",
          "outputVersion": "1",
          "output": {
            "rsrPercentageScore": 50.1234,
            "rsrScoreLevel": "MEDIUM",
            "ospcPercentageScore": 1.07,
            "ospcScoreLevel": "MEDIUM",
            "ospiPercentageScore": 2.81,
            "ospiScoreLevel": "MEDIUM",
            "staticOrDynamic": "DYNAMIC",
            "algorithmVersion": "5"
          }
        },
        {
          "completedDate": "2022-06-09T15:16:21",
          "source": "OASYS",
          "status": "COMPLETE",
          "outputVersion": "1",
          "output": {
            "rsrPercentageScore": 4.12,
            "rsrScoreLevel": "MEDIUM",
            "ospcPercentageScore": 1.07,
            "ospcScoreLevel": "MEDIUM",
            "ospiPercentageScore": 2.81,
            "ospiScoreLevel": "MEDIUM",
            "staticOrDynamic": "DYNAMIC",
            "algorithmVersion": "3"
          }
        },
        {
          "completedDate": "2022-04-27T12:46:39",
          "source": "OASYS",
          "status": "COMPLETE",
          "outputVersion": "1",
          "output": {
            "rsrPercentageScore": 0.32,
            "rsrScoreLevel": "LOW",
            "staticOrDynamic": "STATIC",
            "algorithmVersion": "3"
          }
        }
      ]
    """.trimIndent()
  }
}
