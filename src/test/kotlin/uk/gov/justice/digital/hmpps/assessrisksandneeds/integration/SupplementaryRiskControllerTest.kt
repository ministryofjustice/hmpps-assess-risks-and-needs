package uk.gov.justice.digital.hmpps.assessrisksandneeds.integration

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.http.HttpStatus
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlConfig
import org.springframework.test.context.jdbc.SqlGroup
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.CreateSupplementaryRiskDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.RedactedOasysRiskDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.Source
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.AuditService
import java.time.LocalDateTime

@AutoConfigureWebTestClient
@DisplayName("Supplementary Risk Tests")
@SqlGroup(
  Sql(
    scripts = ["classpath:supplementaryrisk/before-test.sql"],
    config = SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED),
  ),
  Sql(
    scripts = ["classpath:supplementaryrisk/after-test.sql"],
    config = SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED),
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
  ),
)
class SupplementaryRiskControllerTest : IntegrationTestBase() {

  @MockkBean
  private lateinit var auditService: AuditService

  private val supplementaryRiskUuid = "2e020e78-a81c-407f-bc78-e5f284e237e5"
  private val invalidSupplementaryRiskUuid = "2e020e78-a80a-407f-bc78-e5f284e237e5"

  @BeforeEach
  fun setup() {
    every { auditService.sendEvent(any(), any()) } returns Unit
  }

  @Nested
  @DisplayName("Get by Supplementary Risk ID Security")
  inner class GetByIdSecurity {

    @Test
    fun `access forbidden when no authority`() {
      webTestClient.get().uri("/risks/supplementary/$supplementaryRiskUuid")
        .header("Content-Type", "application/json")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `access forbidden when no role`() {
      webTestClient.get().uri("/risks/supplementary/$supplementaryRiskUuid")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isForbidden
        .expectBody<String>()
        .consumeWith {
          JSONAssert.assertEquals(
            """{"status":403,"developerMessage":"Access Denied"}""",
            checkNotNull(it.responseBody),
            JSONCompareMode.STRICT,
          )
        }
    }

    @Test
    fun `not found when supplementary risk uuid doesn't exists`() {
      webTestClient.get().uri("/risks/supplementary/$invalidSupplementaryRiskUuid")
        .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
        .exchange()
        .expectStatus().isNotFound
        .expectBody<String>()
        .consumeWith {
          JSONAssert.assertEquals(
            """{"status":404,"developerMessage":"Error retrieving Supplementary Risk for supplementaryRiskUuid: 2e020e78-a80a-407f-bc78-e5f284e237e5"}""",
            checkNotNull(it.responseBody),
            JSONCompareMode.STRICT,
          )
        }
    }

    @Test
    fun `access allowed when role ROLE_PROBATION and scope supplied`() {
      webTestClient.get().uri("/risks/supplementary/$supplementaryRiskUuid")
        .headers(setAuthorisation(roles = listOf("ROLE_PROBATION")))
        .exchange()
        .expectStatus().isOk
        .expectBody<String>()
        .consumeWith {
          JSONAssert.assertEquals(
            """
              {
                "supplementaryRiskId": "2e020e78-a81c-407f-bc78-e5f284e237e5",
                "source": "INTERVENTION_REFERRAL",
                "sourceId": "182987872",
                "crn": "X123458",
                "createdByUser": "Gary cooper",
                "createdByUserType": "delius",
                "createdDate": "2019-11-14T09:00:00",
                "riskSummaryComments": "risk for children"
              }
            """.trimIndent(),
            checkNotNull(it.responseBody),
            JSONCompareMode.STRICT,
          )
        }
    }

    @Test
    fun `access allowed when role ROLE_CRS_PROVIDER and scope supplied`() {
      webTestClient.get().uri("/risks/supplementary/$supplementaryRiskUuid")
        .headers(setAuthorisation(roles = listOf("ROLE_CRS_PROVIDER")))
        .exchange()
        .expectStatus().isOk
        .expectBody<String>()
        .consumeWith {
          JSONAssert.assertEquals(
            """
              {
                "supplementaryRiskId": "2e020e78-a81c-407f-bc78-e5f284e237e5",
                "source": "INTERVENTION_REFERRAL",
                "sourceId": "182987872",
                "crn": "X123458",
                "createdByUser": "Gary cooper",
                "createdByUserType": "delius",
                "createdDate": "2019-11-14T09:00:00",
                "riskSummaryComments": "risk for children"
              }
            """.trimIndent(),
            checkNotNull(it.responseBody),
            JSONCompareMode.STRICT,
          )
        }
    }
  }

  @Nested
  @DisplayName("Create new supplementary risk")
  inner class PostNewSupplementaryRisk {

    private val requestBody = CreateSupplementaryRiskDto(
      source = Source.INTERVENTION_REFERRAL,
      sourceId = "8e020e78-a81c-407f-bc78-e5f284e237e8",
      crn = "X123457",
      createdByUserType = "delius",
      createdDate = LocalDateTime.of(2019, 11, 14, 9, 7),
      redactedRisk = null,
      riskSummaryComments = "risk to others",
    )

    @Test
    fun `access forbidden when no authority`() {
      webTestClient.post().uri("/risks/supplementary")
        .header("Content-Type", "application/json")
        .bodyValue(requestBody)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `access forbidden when no role`() {
      webTestClient.post().uri("/risks/supplementary")
        .header("Content-Type", "application/json")
        .headers(setAuthorisation())
        .bodyValue(requestBody)
        .exchange()
        .expectStatus().isForbidden
        .expectBody<String>()
        .consumeWith {
          JSONAssert.assertEquals(
            """{"status":403,"developerMessage":"Access Denied"}""",
            checkNotNull(it.responseBody),
            JSONCompareMode.STRICT,
          )
        }
    }

    @Test
    fun `access allowed when role ROLE_PROBATION and scope supplied`() {
      webTestClient.post().uri("/risks/supplementary")
        .header("Content-Type", "application/json")
        .headers(setAuthorisation(user = "Tom C", roles = listOf("ROLE_PROBATION")))
        .bodyValue(requestBody)
        .exchange()
        .expectBody<String>()
        .consumeWith {
          val responseBody = checkNotNull(it.responseBody)
          JSONAssert.assertEquals(
            """
              {
                "source": "INTERVENTION_REFERRAL",
                "sourceId": "8e020e78-a81c-407f-bc78-e5f284e237e8",
                "crn": "X123457",
                "createdByUser": "Tom C",
                "createdByUserType": "delius",
                "createdDate": "2019-11-14T09:07:00",
                "riskSummaryComments": "risk to others"
              }
            """.trimIndent(),
            responseBody.replace(Regex(""""supplementaryRiskId":"[^"]+",\s*"""), ""),
            JSONCompareMode.STRICT,
          )
        }
    }

    @Test
    fun `unauthorized for role ROLE_CRS_PROVIDER and scope supplied`() {
      webTestClient.post().uri("/risks/supplementary")
        .header("Content-Type", "application/json")
        .headers(setAuthorisation(roles = listOf("ROLE_CRS_PROVIDER")))
        .bodyValue(requestBody)
        .exchange()
        .expectStatus().isForbidden
        .expectBody<String>()
        .consumeWith {
          JSONAssert.assertEquals(
            """{"status":403,"developerMessage":"Access Denied"}""",
            checkNotNull(it.responseBody),
            JSONCompareMode.STRICT,
          )
        }
    }

    @Test
    fun `409 returned when record for source already exists`() {
      val requestBody = CreateSupplementaryRiskDto(
        source = Source.INTERVENTION_REFERRAL,
        sourceId = "3e020e78-a81c-407f-bc78-e5f284e237e5",
        crn = "X123457",
        riskSummaryComments = "risk to others",
        createdByUserType = "delius",
        createdDate = LocalDateTime.parse("2024-12-25T12:00:00"),
      )

      webTestClient.post().uri("/risks/supplementary")
        .header("Content-Type", "application/json")
        .headers(setAuthorisation(user = "Gary C", roles = listOf("ROLE_PROBATION")))
        .bodyValue(requestBody)
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.CONFLICT)
        .expectBody<String>()
        .consumeWith {
          JSONAssert.assertEquals(
            """
              {
                "supplementaryRiskId": "4e020e78-a81c-407f-bc78-e5f284e237e5",
                "source": "INTERVENTION_REFERRAL",
                "sourceId": "3e020e78-a81c-407f-bc78-e5f284e237e5",
                "crn": "X123457",
                "createdByUser": "Gary C",
                "createdByUserType": "delius",
                "createdDate": "2019-11-14T09:05:00",
                "riskSummaryComments": "risk to self"
              }
            """.trimIndent(),
            checkNotNull(it.responseBody),
            JSONCompareMode.STRICT,
          )
        }
    }

    @Test
    fun `create new supplementary risk with redacted data`() {
      val redactedRiskBody = requestBody.copy(
        redactedRisk = RedactedOasysRiskDto(
          riskWho = "Risk to person",
          riskWhen = "When risk is greatest",
          riskNature = "Nature is risk",
          concernsSelfHarm = "Self harm concerns",
          concernsSuicide = "Suicide concerns",
          concernsHostel = "Hostel concerns",
          concernsVulnerability = "Vulnerability concerns",
        ),
      )
      webTestClient.post().uri("/risks/supplementary")
        .header("Content-Type", "application/json")
        .headers(setAuthorisation(user = "Tom C", roles = listOf("ROLE_PROBATION")))
        .bodyValue(redactedRiskBody)
        .exchange()
        .expectBody<String>()
        .consumeWith {
          val responseBody = checkNotNull(it.responseBody)
          JSONAssert.assertEquals(
            """
              {
                "source": "INTERVENTION_REFERRAL",
                "sourceId": "8e020e78-a81c-407f-bc78-e5f284e237e8",
                "crn": "X123457",
                "createdByUser": "Tom C",
                "createdByUserType": "delius",
                "createdDate": "2019-11-14T09:07:00",
                "redactedRisk": {
                  "riskWho": "Risk to person",
                  "riskWhen": "When risk is greatest",
                  "riskNature": "Nature is risk",
                  "concernsSelfHarm": "Self harm concerns",
                  "concernsSuicide": "Suicide concerns",
                  "concernsHostel": "Hostel concerns",
                  "concernsVulnerability": "Vulnerability concerns"
                },
                "riskSummaryComments": "risk to others"
              }
            """.trimIndent(),
            responseBody.replace(Regex(""""supplementaryRiskId":"[^"]+",\s*"""), ""),
            JSONCompareMode.STRICT,
          )
        }
    }
  }
}
