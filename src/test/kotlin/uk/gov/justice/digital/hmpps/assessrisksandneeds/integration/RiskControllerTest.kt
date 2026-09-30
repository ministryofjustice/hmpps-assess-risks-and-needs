package uk.gov.justice.digital.hmpps.assessrisksandneeds.integration

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.AuditService

@AutoConfigureWebTestClient
@DisplayName("Risk Tests")
class RiskControllerTest : IntegrationTestBase() {

  @MockkBean
  private lateinit var auditService: AuditService

  private val crn = "X123456"

  @BeforeEach
  fun setup() {
    every { auditService.sendEvent(any(), any()) } returns Unit
  }

  @Test
  fun `get risk summary by crn for external provider`() {
    webTestClient.get().uri("/risks/crn/$crn/summary")
      .headers(setAuthorisation(roles = listOf("ROLE_CRS_PROVIDER")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .consumeWith {
        JSONAssert.assertEquals(externalSummaryJson, checkNotNull(it.responseBody), JSONCompareMode.STRICT)
      }
  }

  @Test
  fun `get risk summary by crn for external provider within timeframe`() {
    val timeframe = 65L
    webTestClient.get().uri("/risks/crn/$crn/summary/$timeframe")
      .headers(setAuthorisation(roles = listOf("ROLE_CRS_PROVIDER")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .consumeWith {
        JSONAssert.assertEquals(externalSummaryJson, checkNotNull(it.responseBody), JSONCompareMode.STRICT)
      }
  }

  @Test
  fun `get risk summary by crn with timeframe query param matches the deprecated path variant`() {
    val timeframe = 65L
    val fromQueryParam = webTestClient.get().uri("/risks/crn/$crn/summary?timeframe=$timeframe")
      .headers(setAuthorisation(roles = listOf("ROLE_CRS_PROVIDER")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody

    val fromPath = webTestClient.get().uri("/risks/crn/$crn/summary/$timeframe")
      .headers(setAuthorisation(roles = listOf("ROLE_CRS_PROVIDER")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody

    JSONAssert.assertEquals(checkNotNull(fromQueryParam), checkNotNull(fromPath), JSONCompareMode.STRICT)
  }

  @Test
  fun `get all risks by crn with timeframe query param matches the deprecated path variant`() {
    val timeframe = 80L
    val fromQueryParam = webTestClient.get().uri("/risks/crn/$crn?timeframe=$timeframe")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody

    val fromPath = webTestClient.get().uri("/risks/crn/$crn/$timeframe")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody

    JSONAssert.assertEquals(checkNotNull(fromQueryParam), checkNotNull(fromPath), JSONCompareMode.STRICT)
  }

  @Test
  fun `get all risks with fulltext by crn with timeframe query param matches the deprecated path variant`() {
    val timeframe = 70L
    val fromQueryParam = webTestClient.get().uri("/risks/crn/$crn/fulltext?timeframe=$timeframe")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody

    val fromPath = webTestClient.get().uri("/risks/crn/$crn/fulltext/$timeframe")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody

    JSONAssert.assertEquals(checkNotNull(fromQueryParam), checkNotNull(fromPath), JSONCompareMode.STRICT)
  }

  @Test
  fun `get all risks by crn with a timeframe query param that excludes all assessments returns no assessed date`() {
    val timeframe = 2L
    webTestClient.get().uri("/risks/crn/$crn?timeframe=$timeframe")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .consumeWith {
        JSONAssert.assertEquals(emptyAllRisksJson, checkNotNull(it.responseBody), JSONCompareMode.STRICT)
      }
  }

  @Test
  fun `get risk summary by crn for probation practitioner`() {
    webTestClient.get().uri("/risks/crn/$crn/summary")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .consumeWith {
        JSONAssert.assertEquals(probationSummaryJson, checkNotNull(it.responseBody), JSONCompareMode.STRICT)
      }
  }

  @Test
  fun `get risk for unknown crn returns not found`() {
    webTestClient.get().uri("/risks/crn/RANDOMCRN")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isNotFound
      .expectBody<String>()
      .consumeWith {
        JSONAssert.assertEquals(
          """{"status":404,"developerMessage":"No such offender for CRN: RANDOMCRN"}""",
          checkNotNull(it.responseBody),
          JSONCompareMode.STRICT,
        )
      }
  }

  @Test
  fun `get all risks by crn for external provider`() {
    webTestClient.get().uri("/risks/crn/$crn")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .consumeWith {
        JSONAssert.assertEquals(allRisksJson, checkNotNull(it.responseBody), JSONCompareMode.STRICT)
      }
  }

  @Test
  fun `get all risks by crn for external provider within timeframe`() {
    val timeframe = 80L
    webTestClient.get().uri("/risks/crn/$crn/$timeframe")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .consumeWith {
        JSONAssert.assertEquals(allRisksJson, checkNotNull(it.responseBody), JSONCompareMode.STRICT)
      }
  }

  @Test
  fun `get all risks with fulltext for risk to self by crn for external provider`() {
    webTestClient.get().uri("/risks/crn/$crn/fulltext")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .consumeWith {
        JSONAssert.assertEquals(allRisksJson, checkNotNull(it.responseBody), JSONCompareMode.STRICT)
      }
  }

  @Test
  fun `get all risks with fulltext for risk to self by crn for external provider within timeframe`() {
    val timeframe = 70L
    webTestClient.get().uri("/risks/crn/$crn/fulltext/$timeframe")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .consumeWith {
        JSONAssert.assertEquals(allRisksJson, checkNotNull(it.responseBody), JSONCompareMode.STRICT)
      }
  }

  @Test
  fun `allow null rosh scores`() {
    val crn = "X234567"
    webTestClient.get().uri("/risks/crn/$crn")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .consumeWith {
        JSONAssert.assertEquals(nullRoshScoresJson, checkNotNull(it.responseBody), JSONCompareMode.STRICT)
      }
  }

  @Test
  fun `get risk management plans by crn`() {
    webTestClient.get().uri("/risks/crn/$crn/risk-management-plan")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .consumeWith {
        JSONAssert.assertEquals(riskManagementPlansJson, checkNotNull(it.responseBody), JSONCompareMode.STRICT)
      }
  }

  @Test
  fun `should return forbidden when user cannot access crn`() {
    webTestClient.get().uri("/risks/crn/FORBIDDEN/risk-management-plan")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `should return not found when Delius cannot find crn`() {
    webTestClient.get().uri("/risks/crn/USER_ACCESS_NOT_FOUND/risk-management-plan")
      .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
      .exchange()
      .expectStatus().isNotFound
      .expectBody<String>()
      .consumeWith {
        JSONAssert.assertEquals(
          """{"status":404,"developerMessage":"No such offender for CRN: USER_ACCESS_NOT_FOUND"}""",
          checkNotNull(it.responseBody),
          JSONCompareMode.STRICT,
        )
      }
  }

  private companion object {
    val externalSummaryJson = """
      {
        "riskInCommunity": {
          "LOW": ["Children", "Known Adult"],
          "MEDIUM": ["Public"],
          "HIGH": ["Staff"]
        },
        "overallRiskLevel": "VERY_HIGH"
      }
    """.trimIndent()

    val probationSummaryJson = """
      {
        "whoIsAtRisk": "whoisAtRisk",
        "natureOfRisk": "natureOfRisk",
        "riskImminence": "riskImminence",
        "riskIncreaseFactors": "riskIncreaseFactors",
        "riskMitigationFactors": "riskMitigationFactors",
        "analysisOfRiskFactors": "analysisOfRiskFactors",
        "riskInCommunity": {
          "LOW": ["Children", "Known Adult"],
          "MEDIUM": ["Public"],
          "HIGH": ["Staff"]
        },
        "riskInCustody": {
          "LOW": ["Children", "Public", "Known Adult"],
          "HIGH": ["Prisoners"],
          "VERY_HIGH": ["Staff"]
        },
        "overallRiskLevel": "VERY_HIGH",
        "assessedOn": "2024-12-19T16:57:25"
      }
    """.trimIndent()

    val emptyAllRisksJson = """{"riskToSelf":{},"otherRisks":{},"summary":{"riskInCommunity":{},"riskInCustody":{}}}"""

    val nullRoshScoresJson = """
      {
        "riskToSelf": {
          "suicide": {},
          "selfHarm": {},
          "custody": {},
          "hostelSetting": {},
          "vulnerability": {}
        },
        "otherRisks": {},
        "summary": {
          "whoIsAtRisk": "whoisAtRisk",
          "natureOfRisk": "natureOfRisk",
          "riskImminence": "riskImminence",
          "riskIncreaseFactors": "riskIncreaseFactors",
          "riskMitigationFactors": "riskMitigationFactors",
          "analysisOfRiskFactors": "analysisOfRiskFactors",
          "riskInCommunity": {
            "MEDIUM": ["Public"],
            "LOW": ["Known Adult"]
          },
          "riskInCustody": {
            "LOW": ["Public", "Known Adult"]
          },
          "overallRiskLevel": "MEDIUM"
        },
        "assessedOn": "2024-12-19T16:57:25"
      }
    """.trimIndent()

    val allRisksJson = """
      {
        "riskToSelf": {
          "suicide": {
            "risk": "YES",
            "previous": "YES",
            "current": "YES",
            "currentConcernsText": "Suicide and/or Self-harm current concerns"
          },
          "selfHarm": {"risk": "DK"},
          "custody": {
            "risk": "YES",
            "previous": "YES",
            "previousConcernsText": "Coping in custody / hostel setting previous concerns",
            "current": "NA"
          },
          "hostelSetting": {
            "risk": "YES",
            "previous": "DK",
            "current": "NO"
          },
          "vulnerability": {
            "risk": "YES",
            "previous": "YES",
            "previousConcernsText": "Vulnerability previous concerns free text",
            "current": "YES",
            "currentConcernsText": "Vulnerability current concerns free text"
          }
        },
        "otherRisks": {
          "escapeOrAbscond": "YES",
          "controlIssuesDisruptiveBehaviour": "YES",
          "breachOfTrust": "DK",
          "riskToOtherPrisoners": "YES"
        },
        "summary": {
          "whoIsAtRisk": "whoisAtRisk",
          "natureOfRisk": "natureOfRisk",
          "riskImminence": "riskImminence",
          "riskIncreaseFactors": "riskIncreaseFactors",
          "riskMitigationFactors": "riskMitigationFactors",
          "analysisOfRiskFactors": "analysisOfRiskFactors",
          "riskInCommunity": {
            "LOW": ["Children", "Known Adult"],
            "MEDIUM": ["Public"],
            "HIGH": ["Staff"]
          },
          "riskInCustody": {
            "LOW": ["Children", "Public", "Known Adult"],
            "HIGH": ["Prisoners"],
            "VERY_HIGH": ["Staff"]
          },
          "overallRiskLevel": "VERY_HIGH"
        },
        "assessedOn": "2024-12-19T16:57:25"
      }
    """.trimIndent()

    val riskManagementPlansJson = """
      {
        "crn": "X123456",
        "limitedAccessOffender": false,
        "riskManagementPlan": [
          {
            "assessmentId": 667025,
            "dateCompleted": "2020-03-26T12:47:17",
            "initiationDate": "2020-03-26T12:38:57",
            "assessmentStatus": "COMPLETE",
            "assessmentType": "LAYER3"
          },
          {
            "assessmentId": 668025,
            "dateCompleted": "2020-03-26T13:00:00",
            "initiationDate": "2020-03-26T12:50:34",
            "assessmentStatus": "COMPLETE",
            "assessmentType": "LAYER3"
          },
          {
            "assessmentId": 673025,
            "dateCompleted": "2020-04-03T11:42:01",
            "initiationDate": "2020-04-03T11:33:00",
            "assessmentStatus": "COMPLETE",
            "assessmentType": "LAYER3"
          },
          {
            "assessmentId": 674025,
            "dateCompleted": "2020-11-02T14:49:39",
            "partcompStatus": "Unsigned",
            "initiationDate": "2020-06-25T13:04:56",
            "assessmentStatus": "LOCKED_INCOMPLETE",
            "assessmentType": "LAYER3"
          },
          {
            "assessmentId": 676026,
            "dateCompleted": "2020-11-05T10:56:37",
            "initiationDate": "2020-11-02T14:50:02",
            "assessmentStatus": "COMPLETE",
            "assessmentType": "LAYER3",
            "superStatus": "COMPLETE",
            "keyInformationCurrentSituation": "Key considerations",
            "furtherConsiderationsCurrentSituation": "Kelvin Brown is currently in the community having received a Adjourned - Other Report on the 01/01/2010 for 12 months\r\rThe end of their sentence is currently unknown. \r\rThey have no areas linked to harm. \r\rKelvin Brown has been assessed as medium risk to the public.\r\rKelvin Brown will have contact with a child on the protection register or in local authority care.\rThey are quite motivated to address offending behaviour.",
            "monitoringAndControl": "3. Added measures for specific risks. Include here all activity aimed at addressing victim perspective and contact.",
            "interventionsAndTreatment": "5. Additional conditions/requirements to manage the specific risks.",
            "victimSafetyPlanning": "7. Contingency",
            "laterWIPAssessmentExists": false,
            "latestWIPDate": "2022-07-21T15:43:58",
            "laterSignLockAssessmentExists": false,
            "laterPartCompUnsignedAssessmentExists": false,
            "latestPartCompUnsignedDate": "2022-05-31T10:37:05",
            "laterPartCompSignedAssessmentExists": false,
            "laterCompleteAssessmentExists": false,
            "latestCompleteDate": "2022-07-21T15:43:12"
          }
        ]
      }
    """.trimIndent()
  }
}
